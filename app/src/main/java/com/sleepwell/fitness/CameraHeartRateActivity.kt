package com.sleepwell.fitness

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.sleepwell.databinding.ActivityCameraHeartRateBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

/**
 * Activity that uses camera PPG (photoplethysmography) to measure heart rate.
 * User places fingertip over back camera with flash on.
 */
class CameraHeartRateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraHeartRateBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var heartRateAnalyzer: HeartRateAnalyzer

    private var camera: Camera? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var lastAnalysisTime = 0L
    private val targetFps = 30.0
    private val minFrameInterval = (1000.0 / targetFps).toLong()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Snackbar.make(
                binding.root,
                "La permission caméra est nécessaire pour mesurer la fréquence cardiaque",
                Snackbar.LENGTH_LONG
            ).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraHeartRateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        heartRateAnalyzer = HeartRateAnalyzer()

        setupUI()
        checkCameraPermission()
    }

    private fun setupUI() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnReset.setOnClickListener {
            resetMeasurement()
        }

        // Observe heart rate results
        lifecycleScope.launch {
            heartRateAnalyzer.heartRate.collectLatest { result ->
                runOnUiThread {
                    updateUI(result)
                }
            }
        }

        // Update progress
        lifecycleScope.launch {
            while (true) {
                kotlinx.coroutines.delay(100)
                val progress = (heartRateAnalyzer.getProgress() * 100).roundToInt()
                runOnUiThread {
                    binding.progressBar.progress = progress
                    binding.tvProgress.text = "$progress%"
                }
            }
        }
    }

    private fun updateUI(result: HeartRateAnalyzer.HeartRateResult?) {
        if (result == null) {
            binding.tvBpm.text = "--"
            binding.tvConfidence.text = "En attente..."
            binding.btnReset.visibility = View.GONE
            return
        }

        if (result.beatsPerMinute > 0) {
            binding.tvBpm.text = result.beatsPerMinute.roundToInt().toString()

            val confidencePercent = (result.confidence * 100).roundToInt()
            binding.tvConfidence.text = "Confiance: $confidencePercent%"

            // Show confidence color
            val confidenceColor = when {
                result.confidence >= 0.7f -> ContextCompat.getColor(this, android.R.color.holo_green_dark)
                result.confidence >= 0.5f -> ContextCompat.getColor(this, android.R.color.holo_orange_dark)
                else -> ContextCompat.getColor(this, android.R.color.holo_red_dark)
            }
            binding.tvConfidence.setTextColor(confidenceColor)

            binding.btnReset.visibility = View.VISIBLE
        } else {
            binding.tvBpm.text = "--"
            binding.tvConfidence.text = "Signal insuffisant"
            binding.tvConfidence.setTextColor(
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
            )
        }
    }

    private fun resetMeasurement() {
        heartRateAnalyzer.reset()
        binding.btnReset.visibility = View.GONE
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Snackbar.make(
                    binding.root,
                    "La caméra est nécessaire pour analyser le flux sanguin dans votre doigt",
                    Snackbar.LENGTH_LONG
                ).setAction("Autoriser") {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }.show()
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

            // Use back camera with flash
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Preview use case
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // Image analysis use case
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, RedChannelAnalyzer())
                }

            try {
                cameraProvider.unbindAll()

                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                // Turn on flash/torch
                camera?.cameraControl?.enableTorch(true)

            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed", e)
                Snackbar.make(
                    binding.root,
                    "Erreur de démarrage de la caméra: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private inner class RedChannelAnalyzer : ImageAnalysis.Analyzer {
        @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            // Rate limiting to target FPS
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastAnalysisTime < minFrameInterval) {
                imageProxy.close()
                return
            }
            lastAnalysisTime = currentTime

            try {
                // Convert to bitmap and extract red channel average
                val bitmap = imageProxy.toBitmap()
                val redAverage = extractRedChannelAverage(bitmap)

                // Add measurement to analyzer
                heartRateAnalyzer.addMeasurement(redAverage)

            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed", e)
            } finally {
                imageProxy.close()
            }
        }

        private fun extractRedChannelAverage(bitmap: Bitmap): Float {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            var redSum = 0L
            for (pixel in pixels) {
                val red = (pixel shr 16) and 0xFF
                redSum += red
            }

            return redSum.toFloat() / pixels.size
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        camera?.cameraControl?.enableTorch(false)
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraHeartRateActivity"
    }
}
