package com.sleepwell.ui.main

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationBarView
import com.sleepwell.R
import com.sleepwell.databinding.ActivityMainBinding
import com.sleepwell.ui.main.dashboard.DashboardFragment
import com.sleepwell.ui.main.goals.GoalsFragment
import com.sleepwell.ui.main.profile.ProfileFragment
import com.sleepwell.ui.main.tips.TipsFragment
import com.sleepwell.utils.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentUserId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get current user ID
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        currentUserId = prefs.getLong(Constants.PREF_USER_ID, -1)

        setupBottomNavigation()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_dashboard -> DashboardFragment()
                R.id.nav_goals -> GoalsFragment()
                R.id.nav_tips -> TipsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> return@OnItemSelectedListener false
            }
            loadFragment(fragment)
            true
        })
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out
            )
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun getUserId(): Long = currentUserId
}
