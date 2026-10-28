package com.xmediatv.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvClock: TextView
    private lateinit var btnFav1: Button
    private lateinit var btnFav2: Button
    private lateinit var btnFav3: Button
    private lateinit var appsRecyclerView: RecyclerView

    data class AppItem(val name: String, val packageName: String, val icon: android.graphics.drawable.Drawable?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvClock = findViewById(R.id.tvClock)
        btnFav1 = findViewById(R.id.btnFav1)
        btnFav2 = findViewById(R.id.btnFav2)
        btnFav3 = findViewById(R.id.btnFav3)
        appsRecyclerView = findViewById(R.id.appsRecyclerView)

        updateClock()
        setupFavorites()
        loadInstalledApps()
    }

    private fun updateClock() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        tvClock.text = sdf.format(Date())
    }

    private fun setupFavorites() {
        // Кнопка 1: LAMPA (Пакет по умолчанию или поиск)
        btnFav1.setOnClickListener {
            launchAppByPackage("com.lampa.tv") ?: launchAppByKeyword("lampa")
        }

        // Кнопка 2: IPTV
        btnFav2.setOnClickListener {
            launchAppByKeyword("iptv") ?: launchAppByKeyword("ott")
        }

        // Кнопка 3: YouTube
        btnFav3.setOnClickListener {
            launchAppByPackage("com.google.android.youtube.tv") ?: launchAppByPackage("com.google.android.youtube")
        }
    }

    private fun launchAppByPackage(packageName: String): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
            return true
        }
        return false
    }

    private fun launchAppByKeyword(keyword: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        for (info in resolveInfos) {
            val appName = info.loadLabel(packageManager).toString().lowercase()
            if (appName.contains(keyword)) {
                val pkg = info.activityInfo.packageName
                val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    startActivity(launchIntent)
                    return true
                }
            }
        }
        return false
    }

    private fun loadInstalledApps() {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        val appList = mutableListOf<AppItem>()

        for (info in resolveInfos) {
            val name = info.loadLabel(packageManager).toString()
            val pkg = info.activityInfo.packageName
            val icon = info.loadIcon(packageManager)
            // Исключаем само себя из общего списка
            if (pkg != packageName) {
                appList.add(AppItem(name, pkg, icon))
            }
        }

        appsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        appsRecyclerView.adapter = AppsAdapter(appList) { app ->
            launchAppByPackage(app.packageName)
        }
    }

    class AppsAdapter(
        private val apps: List<AppItem>,
        private val onClick: (AppItem) -> Unit
    ) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

        class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val btnApp: Button = view.findViewById(R.id.btnAppIcon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            // Создаем динамическую кнопку для нижнего ряда
            val button = Button(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(160, 80)
                textSize = 12f
                setTextColor(android.graphics.Color.WHITE)
                backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#181B20"))
            }
            return AppViewHolder(button)
        }

        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            val app = apps[position]
            holder.btnApp.text = app.name
            holder.btnApp.setOnClickListener { onClick(app) }
        }

        override fun getItemCount() = apps.size
    }
}
