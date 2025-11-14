package com.sleepwell.ui.main.dashboard

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.sleepwell.R
import com.sleepwell.data.model.SleepSession
import com.sleepwell.databinding.FragmentDashboardBinding
import com.sleepwell.ui.main.MainActivity
import com.sleepwell.utils.*
import com.sleepwell.viewmodel.DashboardViewModel
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel
    private var userId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        userId = (activity as? MainActivity)?.getUserId() ?: -1

        setupListeners()
        observeViewModel()
        loadData()
    }

    private fun setupListeners() {
        binding.fabAddSession.setOnClickListener {
            showAddSessionDialog()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }
    }

    private fun loadData() {
        if (userId != -1L) {
            viewModel.loadDashboardData(userId)
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        viewModel.weeklyStats.observe(viewLifecycleOwner) { sessions ->
            updateChart(sessions)
        }

        viewModel.statistics.observe(viewLifecycleOwner) { stats ->
            binding.tvTotalSessions.text = stats.totalSessions.toString()
            binding.tvAvgDuration.text = String.format("%.1fh", stats.averageDuration)
            binding.tvAvgQuality.text = "${stats.averageQuality.toInt()}%"
            binding.tvTotalHours.text = String.format("%.0fh", stats.totalHours)
        }

        viewModel.aiInsight.observe(viewLifecycleOwner) { insight ->
            if (insight != null) {
                binding.tvAiInsight.text = insight
                binding.cardAiInsight.show()
            } else {
                binding.cardAiInsight.hide()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { showToast(it) }
        }
    }

    private fun updateChart(sessions: List<SleepSession>) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        val last7Days = DateUtils.getLastNDays(7)
        last7Days.forEachIndexed { index, date ->
            val daySession = sessions.find { it.startTime.isSameDay(date) }
            entries.add(BarEntry(index.toFloat(), daySession?.durationHours ?: 0f))
            labels.add(DateUtils.getShortDayOfWeek(date))
        }

        val dataSet = BarDataSet(entries, getString(R.string.hours_of_sleep)).apply {
            color = requireContext().getColor(R.color.primary)
            valueTextColor = Color.BLACK
            valueTextSize = 10f
        }

        binding.chart.apply {
            data = BarData(dataSet)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 12f
                setDrawGridLines(true)
            }
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            animateY(Constants.CHART_ANIMATION_DURATION)
            invalidate()
        }
    }

    private fun showAddSessionDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_sleep_session)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etStartTime = dialog.findViewById<TextInputEditText>(R.id.etStartTime)
        val etEndTime = dialog.findViewById<TextInputEditText>(R.id.etEndTime)
        val etQuality = dialog.findViewById<TextInputEditText>(R.id.etQuality)
        val etNotes = dialog.findViewById<TextInputEditText>(R.id.etNotes)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)

        var startDate: Date? = null
        var endDate: Date? = null

        etStartTime.setOnClickListener {
            showDateTimePicker { date ->
                startDate = date
                etStartTime.setText(date.toFullFormat())
            }
        }

        etEndTime.setOnClickListener {
            showDateTimePicker { date ->
                endDate = date
                etEndTime.setText(date.toFullFormat())
            }
        }

        btnSave.setOnClickListener {
            val start = startDate
            val end = endDate
            val qualityStr = etQuality.text.toString()
            val notes = etNotes.text.toString()

            if (start == null || end == null || qualityStr.isBlank()) {
                showToast(getString(R.string.error_fill_all_fields))
                return@setOnClickListener
            }

            val quality = qualityStr.toIntOrNull()?.clamp(0, 100) ?: 0
            val duration = DateUtils.calculateSleepDuration(start, end)

            val session = SleepSession(
                userId = userId,
                startTime = start,
                endTime = end,
                durationHours = duration,
                quality = quality,
                deepSleepPercentage = Constants.DEEP_SLEEP_PERCENTAGE * 100,
                lightSleepPercentage = Constants.LIGHT_SLEEP_PERCENTAGE * 100,
                remSleepPercentage = Constants.REM_SLEEP_PERCENTAGE * 100,
                notes = notes.ifBlank { null }
            )

            viewModel.addSleepSession(userId, session)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDateTimePicker(onDateTimeSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        onDateTimeSelected(calendar.time)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
