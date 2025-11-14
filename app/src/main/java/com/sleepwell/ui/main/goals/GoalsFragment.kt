package com.sleepwell.ui.main.goals

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.sleepwell.R
import com.sleepwell.databinding.FragmentGoalsBinding
import com.sleepwell.ui.main.MainActivity
import com.sleepwell.utils.Constants
import com.sleepwell.utils.formatHours
import com.sleepwell.utils.hide
import com.sleepwell.utils.show
import com.sleepwell.utils.showToast
import com.sleepwell.viewmodel.GoalsViewModel

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GoalsViewModel
    private var userId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[GoalsViewModel::class.java]
        userId = (activity as? MainActivity)?.getUserId() ?: -1

        setupListeners()
        observeViewModel()
        loadData()
    }

    private fun setupListeners() {
        binding.btnCreateGoal.setOnClickListener {
            showCreateGoalDialog()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }
    }

    private fun loadData() {
        if (userId != -1L) {
            viewModel.loadGoals(userId)
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        viewModel.activeGoal.observe(viewLifecycleOwner) { goal ->
            if (goal != null) {
                binding.cardActiveGoal.show()
                binding.layoutNoGoal.hide()

                binding.tvTargetHours.text = goal.targetHours.formatHours(requireContext())
                binding.tvTargetQuality.text = "${goal.targetQuality}%"
                binding.tvCurrentStreak.text = goal.streak.toString()
                binding.tvBestStreak.text = goal.bestStreak.toString()

                binding.progressBar.progress = goal.streak

                binding.tvStreakDays.text = if (goal.streak > 1) {
                    getString(R.string.days)
                } else {
                    getString(R.string.day)
                }
            } else {
                binding.cardActiveGoal.hide()
                binding.layoutNoGoal.show()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                showToast(it)
                viewModel.resetError()
            }
        }

        viewModel.goalCreated.observe(viewLifecycleOwner) { created ->
            if (created) {
                showToast(getString(R.string.goal_created_success))
                viewModel.resetGoalCreated()
            }
        }
    }

    private fun showCreateGoalDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_set_goal)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val sliderHours = dialog.findViewById<Slider>(R.id.sliderHours)
        val sliderQuality = dialog.findViewById<Slider>(R.id.sliderQuality)
        val tvHoursValue = dialog.findViewById<android.widget.TextView>(R.id.tvHoursValue)
        val tvQualityValue = dialog.findViewById<android.widget.TextView>(R.id.tvQualityValue)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)

        // Set default values
        sliderHours.value = Constants.DEFAULT_GOAL_HOURS
        sliderQuality.value = Constants.DEFAULT_GOAL_QUALITY.toFloat()
        tvHoursValue.text = Constants.DEFAULT_GOAL_HOURS.formatHours(requireContext())
        tvQualityValue.text = "${Constants.DEFAULT_GOAL_QUALITY}%"

        sliderHours.addOnChangeListener { _, value, _ ->
            tvHoursValue.text = value.formatHours(requireContext())
        }

        sliderQuality.addOnChangeListener { _, value, _ ->
            tvQualityValue.text = "${value.toInt()}%"
        }

        btnSave.setOnClickListener {
            val targetHours = sliderHours.value
            val targetQuality = sliderQuality.value.toInt()

            viewModel.createGoal(userId, targetHours, targetQuality)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
