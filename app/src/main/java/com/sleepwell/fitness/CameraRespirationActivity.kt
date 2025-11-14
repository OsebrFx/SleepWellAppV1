package com.sleepwell.fitness

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.sleepwell.databinding.ActivityCameraRespirationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Camera-based respiratory rate measurement using ML Kit Pose Detection.
 *
 * HOW TO TEST:
 * 1. Grant camera permission
 * 2. Place phone on stable surface (table/stand)
 * 3. Position so upper torso/chest is visible in frame
 * 4. Sit still and breathe normally
 * 5. Wait 45 seconds for first measurement
 * 6. Results update every few seconds
 */
class CameraRespirationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraRespirationBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var poseDetector: PoseDetector
    private lateinit var respirationAnalyzer: RespirationAnalyzer

    private var lastPose: Pose? = null
    private var measurementCount = 0
    private val targetFps = 10.0
    private var lastProcessTime = 0L

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraRespirationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize ML Kit Pose Detector (accurate mode)
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()
        poseDetector = PoseDetection.getClient(options)

        // Initialize respiration analyzer
        respirationAnalyzer = RespirationAnalyzer(
            windowSizeSeconds = 45,
            samplingRateHz = targetFps
        )

        setupUI()
        checkCameraPermission()
        observeRespirationRate()
    }

    private fun setupUI() {
        binding.btnReset.setOnClickListener {
            resetMeasurement()
        }

        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.tvInstructions.text = """
            Position your upper torso in frame
            Sit still and breathe normally
            Wait 45 seconds for measurement
        """.trimIndent()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, PoseAnalyzer())
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed", e)
                Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_LONG).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun observeRespirationRate() {
        lifecycleScope.launch {
            respirationAnalyzer.respirationRate.collect { result ->
                withContext(Dispatchers.Main) {
                    if (result != null && result.breathsPerMinute > 0) {
                        binding.tvRespirationRate.text =
                            "%.1f BPM".format(result.breathsPerMinute)
                        binding.tvConfidence.text =
                            "Confidence: %.0f%%".format(result.confidence * 100)
                        binding.progressBar.visibility = View.GONE
                        binding.tvStatus.text = "Measuring..."
                    } else {
                        val progress = respirationAnalyzer.getProgress() * 100
                        binding.tvStatus.text = "Collecting data... (%.0f%%)".format(progress)
                        binding.progressBar.progress = progress.toInt()
                    }
                }
            }
        }
    }

    private fun resetMeasurement() {
        respirationAnalyzer.reset()
        measurementCount = 0
        binding.tvRespirationRate.text = "-- BPM"
        binding.tvConfidence.text = "Confidence: --%"
        binding.tvStatus.text = "Ready to measure"
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.progress = 0
    }

    private inner class PoseAnalyzer : ImageAnalysis.Analyzer {

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val currentTime = System.currentTimeMillis()
            val timeSinceLastProcess = currentTime - lastProcessTime

            // Rate limit to target FPS
            if (timeSinceLastProcess < (1000 / targetFps)) {
                imageProxy.close()
                return
            }

            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                poseDetector.process(image)
                    .addOnSuccessListener { pose ->
                        processPose(pose)
                        lastProcessTime = currentTime
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Pose detection failed", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }

    private fun processPose(pose: Pose) {
        if (!PoseHelper.isPoseValid(pose)) {
            lifecycleScope.launch(Dispatchers.Main) {
                binding.tvStatus.text = "Position torso in frame"
            }
            return
        }

        val torsoY = PoseHelper.extractTorsoYPosition(pose)
        if (torsoY != null) {
            respirationAnalyzer.addMeasurement(torsoY)
            measurementCount++
        }

        lastPose = pose
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        poseDetector.close()
    }

    companion object {
        private const val TAG = "CameraRespiration"
    }
}
