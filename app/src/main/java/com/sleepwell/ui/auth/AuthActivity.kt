package com.sleepwell.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.sleepwell.R
import com.sleepwell.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragmentContainer, LoginFragment())
                setReorderingAllowed(true)
            }
        }
    }

    fun navigateToRegister() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, RegisterFragment())
            addToBackStack(null)
            setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            setReorderingAllowed(true)
        }
    }

    fun navigateToLogin() {
        supportFragmentManager.popBackStack()
    }
}
