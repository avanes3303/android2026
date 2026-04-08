package com.example.myapplication.presentation.home

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.domain.model.HabitDisplayItem
import com.example.myapplication.presentation.ViewModelFactory
import com.example.myapplication.presentation.detail.DetailActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {

    companion object {
        const val TAG = "HomeFragment"
    }

    // Обнуляется в onDestroyView чтобы избежать утечек памяти
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val title = result.data?.getStringExtra(DetailActivity.EXTRA_TITLE) ?: return@registerForActivityResult
            viewModel.completeDay(title)
        }
    }

    private lateinit var challengeAdapter: ChallengeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        setupNameInput()
        setupChallengesList()
        setupAddButton()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadChallenges()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        _binding = null
    }

    private fun setupNameInput() {
        // Кнопка активна только когда поле не пустое
        binding.etName.addTextChangedListener { text ->
            binding.btnStart.isEnabled = !text.isNullOrBlank()
        }

        binding.btnStart.setOnClickListener {
            val name = binding.etName.text?.toString()?.trim() ?: ""
            val greeting = if (name.isNotBlank()) {
                getString(R.string.home_greeting_template, name)
            } else {
                getString(R.string.home_enter_name)
            }
            binding.tvGreeting.text = greeting
            binding.cardGreeting.visibility = View.VISIBLE
        }
    }

    private fun setupChallengesList() {
        challengeAdapter = ChallengeAdapter { item ->
            val intent = DetailActivity.newIntent(
                context = requireContext(),
                emoji = item.emoji,
                title = item.title,
                description = item.description,
                category = item.category,
                targetDays = item.targetDays,
                streakDays = item.streakDays,
                completion = item.completionPercent
            )
            detailLauncher.launch(intent)
        }

        binding.rvChallenges.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = challengeAdapter
        }
    }

    private fun setupAddButton() {
        binding.fabAdd.setOnClickListener {
            val editText = EditText(requireContext()).apply {
                hint = getString(R.string.hint_challenge_name)
                setPadding(64, 32, 64, 32)
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_add_title))
                .setView(editText)
                .setPositiveButton(getString(R.string.dialog_add_ok)) { _, _ ->
                    val name = editText.text.toString().trim()
                    if (name.isNotBlank()) {
                        viewModel.addChallenge(name, "⭐", "Другое")
                    }
                }
                .setNegativeButton(getString(R.string.dialog_add_cancel), null)
                .show()
        }
    }

    private fun observeViewModel() {
        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            challengeAdapter.submitList(challenges)
        }
    }
}
