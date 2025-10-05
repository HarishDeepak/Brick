package com.smartbrick.nfc.fragments

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartbrick.nfc.adapters.AppListAdapter
import com.smartbrick.nfc.databinding.FragmentAppsBinding
import com.smartbrick.nfc.models.AppInfo
import kotlinx.coroutines.*

class AppsFragment : Fragment() {

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private lateinit var appAdapter: AppListAdapter
    private val installedApps = mutableListOf<AppInfo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupPresetModes()
        loadInstalledApps()
    }

    private fun setupRecyclerView() {
        appAdapter = AppListAdapter(installedApps) { app, isBlocked ->
            // Handle app block/unblock
            app.isBlocked = isBlocked
            saveAppSettings()
        }

        binding.recyclerViewApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appAdapter
        }
    }

    private fun setupPresetModes() {
        binding.chipWork.setOnClickListener { applyPreset("work") }
        binding.chipStudy.setOnClickListener { applyPreset("study") }
        binding.chipFamily.setOnClickListener { applyPreset("family") }
        binding.chipSleep.setOnClickListener { applyPreset("sleep") }
    }

    private fun loadInstalledApps() {
        CoroutineScope(Dispatchers.IO).launch {
            val packageManager = requireContext().packageManager
            val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            val userApps = packages.filter { app ->
                (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            }.map { app ->
                AppInfo(
                    name = app.loadLabel(packageManager).toString(),
                    packageName = app.packageName,
                    icon = app.loadIcon(packageManager),
                    isBlocked = false
                )
            }.sortedBy { it.name }

            withContext(Dispatchers.Main) {
                installedApps.clear()
                installedApps.addAll(userApps)
                appAdapter.notifyDataSetChanged()
                loadSavedSettings()
            }
        }
    }

    private fun applyPreset(preset: String) {
        val socialApps = setOf("com.instagram.android", "com.tiktok", "com.twitter.android", 
                              "com.facebook.katana", "com.reddit.frontpage")
        val entertainmentApps = setOf("com.google.android.youtube", "com.netflix.mediaclient",
                                     "com.spotify.music")
        val allDistractingApps = socialApps + entertainmentApps

        when (preset) {
            "work" -> {
                installedApps.forEach { app ->
                    app.isBlocked = app.packageName in socialApps
                }
            }
            "study" -> {
                installedApps.forEach { app ->
                    app.isBlocked = app.packageName in allDistractingApps
                }
            }
            "family" -> {
                installedApps.forEach { app ->
                    app.isBlocked = app.packageName in socialApps || 
                                   app.packageName == "com.android.chrome"
                }
            }
            "sleep" -> {
                installedApps.forEach { app ->
                    app.isBlocked = app.packageName != "com.android.dialer" && 
                                   app.packageName != "com.android.contacts"
                }
            }
        }

        appAdapter.notifyDataSetChanged()
        saveAppSettings()
    }

    private fun saveAppSettings() {
        // Save to SharedPreferences
    }

    private fun loadSavedSettings() {
        // Load from SharedPreferences
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
