package com.sleepwell.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.sleepwell.R
import com.sleepwell.databinding.ActivityOnboardingBinding
import com.sleepwell.ui.auth.AuthActivity
import com.sleepwell.utils.Constants
import com.sleepwell.utils.hide
import com.sleepwell.utils.show

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupListeners()
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
        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < 2) {
                binding.viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishOnboarding()
        }

        binding.btnBack.setOnClickListener {
            if (binding.viewPager.currentItem > 0) {
                binding.viewPager.currentItem -= 1
            }
        }
    }

    private fun updateButtons(position: Int) {
        when (position) {
            0 -> {
                binding.btnBack.hide()
                binding.btnSkip.show()
                binding.btnNext.text = getString(R.string.next)
            }
            1 -> {
                binding.btnBack.show()
                binding.btnSkip.show()
                binding.btnNext.text = getString(R.string.next)
            }
            2 -> {
                binding.btnBack.show()
                binding.btnSkip.hide()
                binding.btnNext.text = getString(R.string.get_started)
            }
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
