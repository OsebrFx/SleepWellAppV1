package com.sleepwell.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.sleepwell.R
import com.sleepwell.databinding.ActivityOnboardingBinding
import com.sleepwell.ui.auth.AuthActivity
import com.sleepwell.utils.Constants

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupListeners()
        setupAnimations()
    }

    private fun setupAnimations() {
        // Initial entrance animations
        binding.topGradient.alpha = 0f
        binding.topGradient.animate()
            .alpha(0.1f)
            .setDuration(600)
            .start()

        binding.viewPager.alpha = 0f
        binding.viewPager.translationY = 100f
        binding.viewPager.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(700)
            .setStartDelay(200)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        binding.indicatorContainer.alpha = 0f
        binding.indicatorContainer.scaleX = 0.8f
        binding.indicatorContainer.scaleY = 0.8f
        binding.indicatorContainer.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setStartDelay(400)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()

        binding.navigationContainer.alpha = 0f
        binding.navigationContainer.translationY = 80f
        binding.navigationContainer.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(500)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        binding.btnSkip.alpha = 0f
        binding.btnSkip.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(300)
            .start()
    }

    private fun setupViewPager() {
        adapter = OnboardingPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtons(position)
            }
        })
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener { view ->
            // Animate button click
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()

            if (binding.viewPager.currentItem < 2) {
                binding.viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishOnboarding()
        }

        binding.btnBack.setOnClickListener { view ->
            // Animate FAB click
            view.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()

            if (binding.viewPager.currentItem > 0) {
                binding.viewPager.currentItem -= 1
            }
        }
    }

    private fun updateButtons(position: Int) {
        when (position) {
            0 -> {
                animateButtonVisibility(binding.btnBack, false)
                animateButtonVisibility(binding.btnSkip, true)
                animateButtonText(binding.btnNext, getString(R.string.next))
            }
            1 -> {
                animateButtonVisibility(binding.btnBack, true)
                animateButtonVisibility(binding.btnSkip, true)
                animateButtonText(binding.btnNext, getString(R.string.next))
            }
            2 -> {
                animateButtonVisibility(binding.btnBack, true)
                animateButtonVisibility(binding.btnSkip, false)
                animateButtonText(binding.btnNext, getString(R.string.get_started))
            }
        }
    }

    private fun animateButtonVisibility(view: View, show: Boolean) {
        if (show) {
            if (view.visibility != View.VISIBLE) {
                view.visibility = View.VISIBLE
                view.alpha = 0f
                view.scaleX = 0.8f
                view.scaleY = 0.8f
                view.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setInterpolator(android.view.animation.OvershootInterpolator())
                    .start()
            }
        } else {
            if (view.visibility == View.VISIBLE) {
                view.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .withEndAction {
                        view.visibility = View.GONE
                    }
                    .start()
            }
        }
    }

    private fun animateButtonText(button: com.google.android.material.button.MaterialButton, newText: String) {
        if (button.text != newText) {
            button.animate()
                .alpha(0f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(150)
                .withEndAction {
                    button.text = newText
                    button.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                }
                .start()
        }
    }

    private fun finishOnboarding() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(Constants.PREF_IS_FIRST_LAUNCH, false).apply()

        startActivity(Intent(this, AuthActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
