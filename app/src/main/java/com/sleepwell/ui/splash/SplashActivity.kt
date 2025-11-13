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

        // Animate logo
        binding.logoImageView.alpha = 0f
        binding.logoImageView.animate()
            .alpha(1f)
            .setDuration(Constants.ANIM_DURATION_LONG)
            .start()

        binding.appNameTextView.alpha = 0f
        binding.appNameTextView.animate()
            .alpha(1f)
            .setDuration(Constants.ANIM_DURATION_LONG)
            .setStartDelay(200)
            .start()

        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2500)
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
