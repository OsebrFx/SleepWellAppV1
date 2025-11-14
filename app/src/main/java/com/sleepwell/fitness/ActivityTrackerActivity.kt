package com.sleepwell.fitness

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.sleepwell.databinding.ActivityActivityTrackerBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Activity for tracking physical activity metrics (steps, distance, calories).
 * Uses device sensors to count steps in real-time.
 */
class ActivityTrackerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActivityTrackerBinding
    private lateinit var activityTracker: ActivityTracker

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            initializeTracker()
        } else {
            showPermissionDeniedMessage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActivityTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkPermissionAndInitialize()
    }

    private fun setupUI() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnStartStop.setOnClickListener {
            if (activityTracker.activityStats.value.isTracking) {
                stopTracking()
            } else {
                startTracking()
            }
        }

        binding.btnReset.setOnClickListener {
            resetTracking()
        }

        // Observe activity stats
        lifecycleScope.launch {
            activityTracker.activityStats.collectLatest { stats ->
                updateUI(stats)
            }
        }
    }

    private fun checkPermissionAndInitialize() {
        // For Android Q (API 29) and above, ACTIVITY_RECOGNITION permission is required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    initializeTracker()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) -> {
                    Snackbar.make(
                        binding.root,
                        getString(com.sleepwell.R.string.activity_permission_rationale),
                        Snackbar.LENGTH_LONG
                    ).setAction("Autoriser") {
                        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    }.show()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            }
        } else {
            // For devices below Android Q, no permission needed
            initializeTracker()
        }
    }

    private fun initializeTracker() {
        activityTracker = ActivityTracker(this)

        if (!activityTracker.isSensorAvailable) {
            showSensorUnavailableMessage()
            return
        }

        binding.statusMessage.visibility = View.GONE
        binding.statsContainer.visibility = View.VISIBLE
        binding.controlsContainer.visibility = View.VISIBLE
    }

    private fun startTracking() {
        val success = activityTracker.startTracking()
        if (!success) {
            Snackbar.make(
                binding.root,
                "Impossible de démarrer le suivi d'activité",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun stopTracking() {
        activityTracker.stopTracking()
    }

    private fun resetTracking() {
        activityTracker.reset()
        Snackbar.make(
            binding.root,
            "Statistiques réinitialisées",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun updateUI(stats: ActivityTracker.ActivityStats) {
        // Update steps
        binding.tvSteps.text = stats.steps.toString()

        // Update distance
        binding.tvDistance.text = ActivityTracker.formatDistance(stats.distanceMeters)

        // Update calories
        binding.tvCalories.text = stats.caloriesBurned.toString()

        // Update button state
        if (stats.isTracking) {
            binding.btnStartStop.text = "Arrêter"
            binding.btnStartStop.setIconResource(com.sleepwell.R.drawable.ic_close)
            binding.trackingIndicator.visibility = View.VISIBLE
        } else {
            binding.btnStartStop.text = "Démarrer"
            binding.btnStartStop.setIconResource(android.R.drawable.ic_media_play)
            binding.trackingIndicator.visibility = View.GONE
        }

        // Show/hide reset button
        binding.btnReset.visibility = if (stats.steps > 0) View.VISIBLE else View.GONE
    }

    private fun showPermissionDeniedMessage() {
        binding.statusMessage.visibility = View.VISIBLE
        binding.statusMessage.text = "Permission d'activité refusée. " +
                "Veuillez activer les permissions dans les paramètres de l'application."
        binding.statsContainer.visibility = View.GONE
        binding.controlsContainer.visibility = View.GONE
    }

    private fun showSensorUnavailableMessage() {
        binding.statusMessage.visibility = View.VISIBLE
        binding.statusMessage.text = "Capteur de pas non disponible sur cet appareil."
        binding.statsContainer.visibility = View.GONE
        binding.controlsContainer.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        // Resume tracking if it was active
        if (::activityTracker.isInitialized &&
            activityTracker.activityStats.value.isTracking) {
            activityTracker.startTracking()
        }
    }

    override fun onPause() {
        super.onPause()
        // Keep tracking in background by not stopping
        // The sensor will continue to count steps even when app is paused
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::activityTracker.isInitialized) {
            activityTracker.stopTracking()
        }
    }
}
