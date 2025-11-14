package com.sleepwell.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sleepwell.R
import com.sleepwell.databinding.FragmentOnboardingPageBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val position = arguments?.getInt(ARG_POSITION, 0) ?: 0
        setupPage(position)
    }

    private fun setupPage(position: Int) {
        when (position) {
            0 -> {
                binding.imageView.setImageResource(R.drawable.ic_onboarding_1)
                binding.titleTextView.text = getString(R.string.onboarding_title_1)
                binding.descriptionTextView.text = getString(R.string.onboarding_desc_1)
            }
            1 -> {
                binding.imageView.setImageResource(R.drawable.ic_onboarding_2)
                binding.titleTextView.text = getString(R.string.onboarding_title_2)
                binding.descriptionTextView.text = getString(R.string.onboarding_desc_2)
            }
            2 -> {
                binding.imageView.setImageResource(R.drawable.ic_onboarding_3)
                binding.titleTextView.text = getString(R.string.onboarding_title_3)
                binding.descriptionTextView.text = getString(R.string.onboarding_desc_3)
            }
        }

        // Animate views
        binding.imageView.alpha = 0f
        binding.titleTextView.alpha = 0f
        binding.descriptionTextView.alpha = 0f

        binding.imageView.animate().alpha(1f).setDuration(500).start()
        binding.titleTextView.animate().alpha(1f).setDuration(500).setStartDelay(200).start()
        binding.descriptionTextView.animate().alpha(1f).setDuration(500).setStartDelay(400).start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int) = OnboardingFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_POSITION, position)
            }
        }
    }
}
