package com.smartbrick.nfc.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.smartbrick.nfc.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        setupViews()
        loadSettings()
    }

    private fun setupViews() {
        // Strict Mode
        binding.switchStrictMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("strict_mode", isChecked).apply()
        }

        // Notifications
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()
        }

        // Auto Unbrick Timer
        binding.seekBarAutoUnbrick.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val minutes = progress * 15 // 15 minute intervals
                binding.autoUnbrickText.text = if (minutes == 0) "Disabled" else "$minutes minutes"
                sharedPrefs.edit().putLong("auto_unbrick_minutes", minutes.toLong()).apply()
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        // Reset Emergency Unbricks
        binding.btnResetUnbricks.setOnClickListener {
            sharedPrefs.edit().putInt("emergency_unbricks", 5).apply()
            updateEmergencyUnbricksDisplay()
        }

        // Statistics
        binding.btnClearStats.setOnClickListener {
            clearStatistics()
        }

        // About
        binding.textVersion.text = "Version 1.0.0"
    }

    private fun loadSettings() {
        binding.switchStrictMode.isChecked = sharedPrefs.getBoolean("strict_mode", false)
        binding.switchNotifications.isChecked = sharedPrefs.getBoolean("notifications_enabled", true)

        val autoUnbrickMinutes = sharedPrefs.getLong("auto_unbrick_minutes", 0).toInt()
        binding.seekBarAutoUnbrick.progress = autoUnbrickMinutes / 15
        binding.autoUnbrickText.text = if (autoUnbrickMinutes == 0) "Disabled" else "$autoUnbrickMinutes minutes"

        updateEmergencyUnbricksDisplay()
        updateStatistics()
    }

    private fun updateEmergencyUnbricksDisplay() {
        val remaining = sharedPrefs.getInt("emergency_unbricks", 5)
        binding.emergencyUnbricksText.text = "$remaining remaining"
    }

    private fun updateStatistics() {
        val totalSessions = sharedPrefs.getInt("total_sessions", 0)
        val totalFocusTime = sharedPrefs.getLong("total_focus_time", 0)
        val currentStreak = sharedPrefs.getInt("current_streak", 0)

        binding.totalSessionsText.text = "$totalSessions sessions"
        binding.totalFocusTimeText.text = formatTime(totalFocusTime)
        binding.currentStreakText.text = "$currentStreak days"
    }

    private fun clearStatistics() {
        sharedPrefs.edit()
            .putInt("total_sessions", 0)
            .putLong("total_focus_time", 0)
            .putInt("current_streak", 0)
            .apply()

        updateStatistics()
    }

    private fun formatTime(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        return "${hours}h ${minutes}m"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
