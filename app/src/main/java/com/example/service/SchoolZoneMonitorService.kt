package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SchoolZoneMonitorService : Service(), LocationListener {

    private var locationManager: LocationManager? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val focusManager = FocusManager.getInstance()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("حارس التركيز قيد التشغيل", "جاري مراقبة الحرم والمؤسسات التعليمية"))

        scope.launch {
            focusManager.focusState.collectLatest { state ->
                val title = if (state.isInsideZone) {
                    "🔒 مقفل للدراسة: ${state.currentInstitution?.nameAr ?: "المؤسسة التعليمية"}"
                } else {
                    "🛡️ حارس التركيز نشط"
                }
                val desc = if (state.isInsideZone) {
                    val mins = state.sessionDurationSeconds / 60
                    val secs = state.sessionDurationSeconds % 60
                    "جلسة التركيز الحالية: %02d:%02d - سيتم فتح الهاتف فور الخروج".format(mins, secs)
                } else {
                    val nearest = state.nearestInstitution
                    if (nearest != null) {
                        "أقرب مؤسسة: ${nearest.nameAr} (${state.distanceToNearestMeters.toInt()}م)"
                    } else {
                        "جاري فحص النطاق الجغرافي للمؤسسات التعليمية"
                    }
                }
                updateNotification(title, desc)
            }
        }

        startLocationTracking()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationTracking() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        try {
            val hasGps = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
            val hasNetwork = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

            if (hasGps) {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000L, // 3 seconds
                    3f,    // 3 meters
                    this
                )
            } else if (hasNetwork) {
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    3000L,
                    3f,
                    this
                )
            }

            // Get last known location
            val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNet = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = lastGps ?: lastNet
            if (best != null && !focusManager.focusState.value.isSimulatedLocation) {
                focusManager.onLocationChanged(best.latitude, best.longitude, false, applicationContext)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        if (!focusManager.focusState.value.isSimulatedLocation) {
            focusManager.onLocationChanged(
                location.latitude,
                location.longitude,
                false,
                applicationContext
            )
        }
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "مراقبة المؤسسات التعليمية والتركيز",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعارات فحص الحرم الجامعي وقفل الهاتف التلقائي"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification(title, content))
    }

    companion object {
        const val CHANNEL_ID = "edufocus_monitor_channel"
        const val NOTIFICATION_ID = 4040

        fun start(context: Context) {
            val intent = Intent(context, SchoolZoneMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, SchoolZoneMonitorService::class.java)
            context.stopService(intent)
        }
    }
}
