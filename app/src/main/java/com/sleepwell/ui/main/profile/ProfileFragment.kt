package com.sleepwell.ui.main.profile

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sleepwell.R
import com.sleepwell.databinding.FragmentProfileBinding
import com.sleepwell.ui.auth.AuthActivity
import com.sleepwell.ui.main.MainActivity
import com.sleepwell.utils.Constants
import com.sleepwell.utils.showToast
import com.sleepwell.viewmodel.ProfileViewModel
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private var userId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        userId = (activity as? MainActivity)?.getUserId() ?: -1

        setupListeners()
        observeViewModel()
        loadData()
    }

    private fun setupListeners() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateDarkMode(userId, isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.switchSleepReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showTimePicker { hour, minute ->
                    viewModel.updateSleepReminder(userId, true, hour, minute)
                }
            } else {
                viewModel.updateSleepReminder(userId, false, 22, 0)
            }
        }

        binding.switchWakeupReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showTimePicker { hour, minute ->
                    viewModel.updateWakeupReminder(userId, true, hour, minute)
                }
            } else {
                viewModel.updateWakeupReminder(userId, false, 7, 0)
            }
        }

        binding.btnExportData.setOnClickListener {
            viewModel.exportDataToCSV(requireContext(), userId)
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadData() {
        if (userId != -1L) {
            viewModel.loadUser(userId)
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUserName.text = it.name
                binding.tvUserEmail.text = it.email
                binding.switchDarkMode.isChecked = it.darkModeEnabled
                binding.switchSleepReminder.isChecked = it.sleepReminderEnabled
                binding.switchWakeupReminder.isChecked = it.wakeupReminderEnabled
            }
        }

        viewModel.exportSuccess.observe(viewLifecycleOwner) { file ->
            file?.let {
                showToast(getString(R.string.export_success))
                shareFile(it)
                viewModel.resetExportSuccess()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                showToast(it)
                viewModel.resetError()
            }
        }
    }

    private fun showTimePicker(onTimeSelected: (hour: Int, minute: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun shareFile(file: java.io.File) {
        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = Constants.EXPORT_MIME_TYPE
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, getString(R.string.share_data)))
        } catch (e: Exception) {
            showToast("Erreur lors du partage: ${e.message}")
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                logout()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun logout() {
        val prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(Constants.PREF_IS_LOGGED_IN, false)
            remove(Constants.PREF_USER_ID)
            apply()
        }

        startActivity(Intent(requireContext(), AuthActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
