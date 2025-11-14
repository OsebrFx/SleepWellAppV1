package com.sleepwell.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.sleepwell.databinding.ActivitySplashBinding
import com.sleepwell.ui.auth.AuthActivity
import com.sleepwell.ui.main.MainActivity
import com.sleepwell.ui.onboarding.OnboardingActivity
import com.sleepwell.utils.Constants

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAnimations()

        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2800)
    }

    private fun setupAnimations() {
        // Animate background circles with rotation and scale
        binding.circleTop.alpha = 0f
        binding.circleTop.scaleX = 0.5f
        binding.circleTop.scaleY = 0.5f
        binding.circleTop.animate()
            .alpha(0.3f)
            .scaleX(1f)
            .scaleY(1f)
            .rotation(180f)
            .setDuration(1500)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        binding.circleBottom.alpha = 0f
        binding.circleBottom.scaleX = 0.5f
        binding.circleBottom.scaleY = 0.5f
        binding.circleBottom.animate()
            .alpha(0.3f)
            .scaleX(1f)
            .scaleY(1f)
            .rotation(-180f)
            .setDuration(1500)
            .setStartDelay(100)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        // Animate logo container with bounce effect
        binding.logoContainer.alpha = 0f
        binding.logoContainer.scaleX = 0.3f
        binding.logoContainer.scaleY = 0.3f
        binding.logoContainer.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setStartDelay(300)
            .setInterpolator(android.view.animation.OvershootInterpolator(1.5f))
            .start()

        // Animate logo image inside container
        binding.logoImageView.alpha = 0f
        binding.logoImageView.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(800)
            .start()

        // Animate app name with slide up
        binding.appNameTextView.alpha = 0f
        binding.appNameTextView.translationY = 50f
        binding.appNameTextView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(700)
            .setStartDelay(1000)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        // Animate tagline
        binding.taglineTextView.alpha = 0f
        binding.taglineTextView.translationY = 30f
        binding.taglineTextView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(1200)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        // Animate progress bar with fade in
        binding.progressBar.alpha = 0f
        binding.progressBar.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(1500)
            .start()
    }

    private fun navigateToNextScreen() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(Constants.PREF_IS_FIRST_LAUNCH, true)
        val isLoggedIn = prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false)

        val intent = when {
            isFirstLaunch -> Intent(this, OnboardingActivity::class.java)
            isLoggedIn -> Intent(this, MainActivity::class.java)
            else -> Intent(this, AuthActivity::class.java)
        }

        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
