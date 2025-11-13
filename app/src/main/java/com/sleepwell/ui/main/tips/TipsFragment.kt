package com.sleepwell.ui.main.tips

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sleepwell.R
import com.sleepwell.data.model.Tip
import com.sleepwell.databinding.FragmentTipsBinding

class TipsFragment : Fragment() {

    private var _binding: FragmentTipsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TipsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadTips()
    }

    private fun setupRecyclerView() {
        adapter = TipsAdapter(requireContext())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TipsFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadTips() {
        val tips = listOf(
            Tip(1, "tip_title_1", "tip_desc_1", "sleep_hygiene", R.drawable.ic_sleep, 1),
            Tip(2, "tip_title_2", "tip_desc_2", "sleep_hygiene", R.drawable.ic_bed, 2),
            Tip(3, "tip_title_3", "tip_desc_3", "lifestyle", R.drawable.ic_lifestyle, 3),
            Tip(4, "tip_title_4", "tip_desc_4", "diet", R.drawable.ic_diet, 4),
            Tip(5, "tip_title_5", "tip_desc_5", "exercise", R.drawable.ic_exercise, 5),
            Tip(6, "tip_title_6", "tip_desc_6", "environment", R.drawable.ic_environment, 6),
            Tip(7, "tip_title_7", "tip_desc_7", "relaxation", R.drawable.ic_relax, 7),
            Tip(8, "tip_title_8", "tip_desc_8", "sleep_hygiene", R.drawable.ic_moon, 8),
            Tip(9, "tip_title_9", "tip_desc_9", "lifestyle", R.drawable.ic_phone, 9),
            Tip(10, "tip_title_10", "tip_desc_10", "relaxation", R.drawable.ic_meditation, 10)
        )

        adapter.submitList(tips)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
