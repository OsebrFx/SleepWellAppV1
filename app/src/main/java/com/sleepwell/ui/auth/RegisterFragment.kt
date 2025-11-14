package com.sleepwell.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sleepwell.R
import com.sleepwell.databinding.FragmentRegisterBinding
import com.sleepwell.ui.main.MainActivity
import com.sleepwell.utils.Constants
import com.sleepwell.utils.hideKeyboard
import com.sleepwell.utils.showToast
import com.sleepwell.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            hideKeyboard()
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val ageStr = binding.etAge.text.toString()

            // Validate password confirmation
            if (password != confirmPassword) {
                showToast(getString(R.string.error_passwords_not_match))
                return@setOnClickListener
            }

            val age = ageStr.toIntOrNull() ?: 0
            viewModel.register(name, email, password, age)
        }

        binding.tvLogin.setOnClickListener {
            (activity as? AuthActivity)?.navigateToLogin()
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
        }

        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { userId ->
                // Save login state
                val prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                    putLong(Constants.PREF_USER_ID, userId)
                    apply()
                }

                showToast(getString(R.string.registration_success))

                // Navigate to MainActivity
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            }.onFailure { exception ->
                showToast(exception.message ?: getString(R.string.error_registration_failed))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
