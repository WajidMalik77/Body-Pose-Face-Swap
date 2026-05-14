package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.addCallback
import androidx.navigation.fragment.NavHostFragment
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.databinding.ActivityHomeBinding
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.core.utils.FunnelAnalytics

class HomeActivity : BaseActivity() {
    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private var lastBackPressedAt: Long = 0L
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val status = if (granted) "granted" else "denied"
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("notif_permission_asked", true)
                .apply()
            android.util.Log.d("NotificationTrace", "POST_NOTIFICATIONS result=$status")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor =
            ContextCompat.getColor(this, R.color.bg_status)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        askNotificationPermissionIfNeeded()
        FunnelAnalytics.logScreenEvent(this, "home", "on_create")

        onBackPressedDispatcher.addCallback(this) {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            val navController = navHostFragment?.navController

            // For nested/detail screens: perform normal back navigation first.
            if (navController?.navigateUp() == true) return@addCallback

            // If we're not on Home root, move back to Home instead of exiting app.
            val currentDestinationId = navController?.currentDestination?.id
            if (currentDestinationId != R.id.homeFragment) {
                navController?.popBackStack(R.id.homeFragment, false)
                return@addCallback
            }

            // Only on Home root: double-back to exit.
            val now = System.currentTimeMillis()
            if (now - lastBackPressedAt < 2000L) {
                finishAffinity()
            } else {
                lastBackPressedAt = now
                Toast.makeText(
                    this@HomeActivity,
                    "Press back again to exit",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        FunnelAnalytics.logScreenEvent(this, "home", "on_destroy")
        super.onDestroy()
    }

    private fun askNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) return

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val alreadyAsked = prefs.getBoolean("notif_permission_asked", false)
        if (alreadyAsked) return

        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

}
