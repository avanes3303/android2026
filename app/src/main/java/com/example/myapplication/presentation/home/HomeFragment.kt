package com.example.myapplication.presentation.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.domain.model.HabitDisplayItem
import com.example.myapplication.presentation.ViewModelFactory
import com.example.myapplication.presentation.detail.DetailActivity

// =============================================================================
// ЛАБА 2: HomeFragment
//
// Демонстрация:
//  - Fragment с собственным layout
//  - Lifecycle: onCreateView, onViewCreated, onDestroyView залогированы
//  - View Binding: _binding / binding паттерн, _binding = null в onDestroyView
//  - TextView с хардкоженным текстом из бизнес-логики
//  - EditText + Button → результат в TextView
//  - TextClock как интерактивный элемент
//  - Переход на DetailActivity через явный Intent с putExtra
// =============================================================================

class HomeFragment : Fragment() {

    companion object {
        const val TAG = "HomeFragment"
    }

    // View Binding: nullable поле + non-null accessor
    // _binding обнуляется в onDestroyView чтобы избежать утечек памяти (Л2)
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var challengeAdapter: ChallengeAdapter

    // ЛАБА 2: Fragment Lifecycle — onCreateView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // ЛАБА 2: Fragment Lifecycle — onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        setupNameInput()
        setupChallengesList()
        observeViewModel()
    }

    // ЛАБА 2: Fragment Lifecycle — onDestroyView + обнуление binding
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        _binding = null   // Обязательно! Предотвращает утечки памяти
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
            // ЛАБА 2: Переход на DetailActivity через явный Intent с putExtra
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
            startActivity(intent)
        }

        binding.rvChallenges.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = challengeAdapter
        }
    }

    private fun observeViewModel() {
        // ЛАБА 5: Fragment ТОЛЬКО наблюдает за данными (observe) и обновляет UI
        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            challengeAdapter.submitList(challenges)
        }
    }
}
