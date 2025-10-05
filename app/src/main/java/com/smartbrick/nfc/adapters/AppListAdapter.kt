package com.smartbrick.nfc.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smartbrick.nfc.databinding.ItemAppBinding
import com.smartbrick.nfc.models.AppInfo

class AppListAdapter(
    private val apps: List<AppInfo>,
    private val onAppToggle: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    class AppViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]

        holder.binding.apply {
            appIcon.setImageDrawable(app.icon)
            appName.text = app.name
            appPackage.text = app.packageName
            switchBlocked.isChecked = app.isBlocked

            switchBlocked.setOnCheckedChangeListener { _, isChecked ->
                onAppToggle(app, isChecked)
            }
        }
    }

    override fun getItemCount() = apps.size
}
