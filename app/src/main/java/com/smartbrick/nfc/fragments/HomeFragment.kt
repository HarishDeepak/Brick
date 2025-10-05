package com.smartbrick.nfc.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.smartbrick.nfc.databinding.FragmentHomeBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPrefs: SharedPreferences
    private var timerJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        setupViews()
        updateUI()
        startTimer()
    }

    private fun setupViews() {
        binding.btnEmergencyUnbrick.setOnClickListener {
            showEmergencyUnbrickDialog()
        }

        binding.cardStats.setOnClickListener {
            // Navigate to detailed stats
        }
    }

    private fun updateUI() {
        val isBricked = sharedPrefs.getBoolean("is_bricked", false)
        val emergencyUnbricks = sharedPrefs.getInt("emergency_unbricks", 5)
        val lastStateChange = sharedPrefs.getLong("last_state_change", 0)
        val totalFocusTime = sharedPrefs.getLong("total_focus_time", 0)
        val currentStreak = sharedPrefs.getInt("current_streak", 0)

        // Update status
        if (isBricked) {
            binding.statusIcon.text = "🔒"
            binding.statusText.text = "BRICKED"
            binding.statusSubtext.text = "Tap your NFC tag to unbrick"
            binding.mainCard.setCardBackgroundColor(
                resources.getColor(android.R.color.holo_red_light, null)
            )
        } else {
            binding.statusIcon.text = "🔓"
            binding.statusText.text = "UNBRICKED"
            binding.statusSubtext.text = "Tap your NFC tag to focus"
            binding.mainCard.setCardBackgroundColor(
                resources.getColor(android.R.color.holo_green_light, null)
            )
        }

        // Update stats
        binding.emergencyUnbricksText.text = "Emergency Unbricks: $emergencyUnbricks"
        binding.totalFocusTime.text = formatDuration(totalFocusTime)
        binding.currentStreak.text = "$currentStreak days"

        // Show/hide emergency button
        binding.btnEmergencyUnbrick.visibility = if (isBricked) View.VISIBLE else View.GONE
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val isBricked = sharedPrefs.getBoolean("is_bricked", false)
                val lastChange = sharedPrefs.getLong("last_state_change", 0)

                if (isBricked && lastChange > 0) {
                    val elapsed = System.currentTimeMillis() - lastChange
                    binding.sessionTimer.text = formatDuration(elapsed)
                } else {
                    binding.sessionTimer.text = "00:00:00"
                }

                delay(1000)
            }
        }
    }

    private fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun showEmergencyUnbrickDialog() {
        // Show emergency unbrick dialog
        val currentUnbricks = sharedPrefs.getInt("emergency_unbricks", 5)
        if (currentUnbricks > 0) {
            // Use one emergency unbrick
            sharedPrefs.edit()
                .putBoolean("is_bricked", false)
                .putInt("emergency_unbricks", currentUnbricks - 1)
                .putLong("last_state_change", System.currentTimeMillis())
                .apply()

            updateUI()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        startTimer()
    }

    override fun onPause() {
        super.onPause()
        timerJob?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        timerJob?.cancel()
    }
}
