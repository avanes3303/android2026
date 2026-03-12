package com.example.myapplication.presentation.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentProfileBinding
import com.example.myapplication.domain.model.ProfileStat
import com.example.myapplication.presentation.ViewModelFactory

class ProfileFragment : Fragment() {

    companion object {
        const val TAG = "ProfileFragment"
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            binding.tvProfileName.text = profile.name
            binding.tvProfileSince.text = getString(R.string.profile_member_since, profile.memberSince)
            populateStats(profile.stats)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        _binding = null
    }

    private fun populateStats(stats: List<ProfileStat>) {
        val container = binding.llStatsContainer
        container.removeAllViews()

        stats.forEachIndexed { index, stat ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val padding = resources.getDimensionPixelSize(R.dimen.spacing_sm)
                setPadding(0, padding, 0, padding)
            }

            val labelView = TextView(requireContext()).apply {
                text = stat.label
                setTextColor(resources.getColor(R.color.on_surface_variant, null))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val valueView = TextView(requireContext()).apply {
                text = stat.value
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            row.addView(labelView)
            row.addView(valueView)
            container.addView(row)

            // Разделитель (кроме последней строки)
            if (index < stats.lastIndex) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                    )
                    setBackgroundColor(resources.getColor(R.color.surface_variant, null))
                }
                container.addView(divider)
            }
        }
    }
}
