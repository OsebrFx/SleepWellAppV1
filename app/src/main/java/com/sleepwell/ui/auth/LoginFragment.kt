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
import com.sleepwell.databinding.FragmentLoginBinding
import com.sleepwell.ui.main.MainActivity
import com.sleepwell.utils.Constants
import com.sleepwell.utils.hideKeyboard
import com.sleepwell.utils.showToast
import com.sleepwell.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            hideKeyboard()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            (activity as? AuthActivity)?.navigateToRegister()
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }

        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { user ->
                // Save login state
                val prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                    putLong(Constants.PREF_USER_ID, user.id)
                    apply()
                }

                // Navigate to MainActivity
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            }.onFailure { exception ->
                showToast(exception.message ?: getString(R.string.error_login_failed))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
