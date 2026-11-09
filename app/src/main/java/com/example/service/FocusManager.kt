package com.example.service

import android.app.Activity
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import com.example.data.InstitutionRepository
import com.example.model.EducationalInstitution
import com.example.model.FocusSession
import com.example.model.FocusState
import com.example.model.UserSettings
import com.example.receiver.EduFocusDeviceAdminReceiver
import com.example.util.ArabicSearchEngine
import com.example.ui.lock.FocusLockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FocusManager private constructor() {

    private val repository = InstitutionRepository.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _focusState = MutableStateFlow(
        FocusState(
            institutionsInMyDelegation = repository.getInstitutionsInDelegation("أكودة")
        )
    )
    val focusState: StateFlow<FocusState> = _focusState.asStateFlow()

    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    private var sessionTimerJob: Job? = null
    private var previousRingerMode: Int = AudioManager.RINGER_MODE_NORMAL

    fun updateSettings(newSettings: UserSettings) {
        _userSettings.value = newSettings
    }

    fun isDeviceAdminActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val adminComponent = ComponentName(context, EduFocusDeviceAdminReceiver::class.java)
        return dpm?.isAdminActive(adminComponent) == true
    }

    fun requestDeviceAdmin(activity: Activity) {
        val adminComponent = ComponentName(activity, EduFocusDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "تفعيل صلاحية قفل الشاشة تلقائياً عند الدخول إلى المؤسسة التعليمية لمنع التشتت أثناء الحصص والمحاضرات."
            )
        }
        activity.startActivity(intent)
    }

    fun toggleProtection(context: Context) {
        val current = _focusState.value.isProtectionActive
        val updated = !current
        _focusState.update { it.copy(isProtectionActive = updated) }

        if (updated) {
            SchoolZoneMonitorService.start(context)
            evaluateLocation(_focusState.value.userLatitude, _focusState.value.userLongitude, context)
        } else {
            if (_focusState.value.isInsideZone) {
                exitZone(context)
            }
            SchoolZoneMonitorService.stop(context)
        }
    }

    fun onLocationChanged(latitude: Double, longitude: Double, isSimulated: Boolean, context: Context) {
        val delInfo = repository.detectDelegationFromCoordinates(latitude, longitude)
        val institutionsInDel = repository.getInstitutionsInDelegation(delInfo.delegation)

        _focusState.update {
            it.copy(
                userLatitude = latitude,
                userLongitude = longitude,
                isSimulatedLocation = isSimulated,
                detectedDelegation = delInfo.delegation,
                detectedState = delInfo.state,
                detectedCountry = delInfo.country,
                institutionsInMyDelegation = institutionsInDel
            )
        }
        evaluateLocation(latitude, longitude, context)
    }

    fun switchDelegationManually(delegationName: String, context: Context) {
        val delInfo = repository.delegationCenters.find {
            it.delegation.equals(delegationName, ignoreCase = true) ||
                    ArabicSearchEngine.normalizeArabic(it.delegation) == ArabicSearchEngine.normalizeArabic(delegationName)
        }
        if (delInfo != null) {
            onLocationChanged(delInfo.centerLat, delInfo.centerLon, true, context)
            Toast.makeText(context, "تم تحديد معتمدية: ${delInfo.delegation} (${delInfo.state})", Toast.LENGTH_SHORT).show()
        }
    }

    fun simulateEnterInstitution(institutionId: String, context: Context) {
        val inst = repository.institutions.value.find { it.id == institutionId }
        if (inst != null) {
            onLocationChanged(inst.latitude, inst.longitude, true, context)
            Toast.makeText(
                context,
                "تمت محاكاة التواجد في: ${inst.nameAr}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun simulateExitInstitution(context: Context) {
        val inst = _focusState.value.currentInstitution ?: repository.institutions.value.firstOrNull()
        if (inst != null) {
            // Move 3 km outside the school radius
            val offsetLat = inst.latitude + 0.03
            val offsetLon = inst.longitude + 0.03
            onLocationChanged(offsetLat, offsetLon, true, context)
            Toast.makeText(
                context,
                "تمت محاكاة الخروج من منطقة الدراسة بنجاح",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun evaluateLocation(lat: Double, lon: Double, context: Context) {
        if (!_focusState.value.isProtectionActive) return

        val institutions = repository.institutions.value
        if (institutions.isEmpty()) return

        var nearestInst: EducationalInstitution? = null
        var minDistance = Float.MAX_VALUE
        var detectedInside: EducationalInstitution? = null

        for (inst in institutions) {
            val dist = InstitutionRepository.calculateDistanceMeters(lat, lon, inst.latitude, inst.longitude)
            if (dist < minDistance) {
                minDistance = dist
                nearestInst = inst
            }
            if (dist <= inst.radiusMeters && inst.autoLockEnabled) {
                detectedInside = inst
                break
            }
        }

        val wasInside = _focusState.value.isInsideZone
        val delInfo = repository.detectDelegationFromCoordinates(lat, lon)
        val institutionsInDel = repository.getInstitutionsInDelegation(delInfo.delegation)

        _focusState.update {
            it.copy(
                nearestInstitution = nearestInst,
                distanceToNearestMeters = minDistance,
                detectedDelegation = delInfo.delegation,
                detectedState = delInfo.state,
                detectedCountry = delInfo.country,
                institutionsInMyDelegation = institutionsInDel
            )
        }

        if (detectedInside != null && !wasInside) {
            enterZone(detectedInside, context)
        } else if (detectedInside == null && wasInside) {
            exitZone(context)
        }
    }

    private fun enterZone(institution: EducationalInstitution, context: Context) {
        val now = System.currentTimeMillis()
        _focusState.update {
            it.copy(
                isInsideZone = true,
                currentInstitution = institution,
                entryTimeMillis = now,
                sessionDurationSeconds = 0L,
                isPhoneLocked = true
            )
        }

        // Apply sound policy
        applySilentMode(context)

        // Haptic feedback
        vibrate(context, longArrayOf(0, 120, 80, 200))

        // Trigger native screen lock if enabled
        if (_userSettings.value.systemAdminLock) {
            triggerNativeScreenLock(context)
        }

        // Trigger strict lock activity if enabled
        if (_userSettings.value.strictOverlayLock) {
            val intent = Intent(context, FocusLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }

        // Start session stopwatch
        sessionTimerJob?.cancel()
        sessionTimerJob = scope.launch {
            while (isActive && _focusState.value.isInsideZone) {
                delay(1000)
                val elapsed = (System.currentTimeMillis() - now) / 1000
                _focusState.update { it.copy(sessionDurationSeconds = elapsed) }
            }
        }
    }

    private fun exitZone(context: Context) {
        sessionTimerJob?.cancel()
        val entryTime = _focusState.value.entryTimeMillis
        val durationMins = if (entryTime > 0) {
            ((System.currentTimeMillis() - entryTime) / 60000L).coerceAtLeast(1L)
        } else 1L

        val inst = _focusState.value.currentInstitution

        if (inst != null) {
            val dateFormat = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault())
            val session = FocusSession(
                id = "sess_${System.currentTimeMillis()}",
                institutionId = inst.id,
                institutionName = inst.nameAr,
                startTimeMillis = entryTime,
                endTimeMillis = System.currentTimeMillis(),
                durationMinutes = durationMins,
                dateFormatted = dateFormat.format(Date())
            )
            repository.addSession(session)
        }

        _focusState.update {
            it.copy(
                isInsideZone = false,
                currentInstitution = null,
                isPhoneLocked = false,
                totalFocusMinutesToday = it.totalFocusMinutesToday + durationMins.toInt(),
                savedDistractionsCount = it.savedDistractionsCount + ((durationMins / 5).toInt().coerceAtLeast(1))
            )
        }

        // Restore sound mode
        restoreSoundMode(context)

        // Send unlock broadcast for FocusLockActivity
        val unlockIntent = Intent(ACTION_ZONE_UNLOCKED)
        context.sendBroadcast(unlockIntent)

        vibrate(context, longArrayOf(0, 80, 50, 80))
        Toast.makeText(context, "خرجت من الحرم التعليمي - تم إلغاء قفل الهاتف بنجاح", Toast.LENGTH_LONG).show()
    }

    fun unlockEmergency(enteredPin: String, context: Context): Boolean {
        if (enteredPin == _userSettings.value.emergencyPin) {
            _focusState.update { it.copy(isPhoneLocked = false) }
            restoreSoundMode(context)
            val unlockIntent = Intent(ACTION_ZONE_UNLOCKED)
            context.sendBroadcast(unlockIntent)
            Toast.makeText(context, "تم الفك الاستثنائي لقفل الدراسة", Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    private fun triggerNativeScreenLock(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val adminComponent = ComponentName(context, EduFocusDeviceAdminReceiver::class.java)
        if (dpm != null && dpm.isAdminActive(adminComponent)) {
            try {
                dpm.lockNow()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applySilentMode(context: Context) {
        if (!_userSettings.value.silentModeEnabled) return
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        previousRingerMode = audioManager.ringerMode
        try {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            _focusState.update { it.copy(isDndActive = true) }
        } catch (_: Exception) {
            try {
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            } catch (_: Exception) {}
        }
    }

    private fun restoreSoundMode(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        try {
            audioManager.ringerMode = previousRingerMode
            _focusState.update { it.copy(isDndActive = false) }
        } catch (_: Exception) {}
    }

    private fun vibrate(context: Context, pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    companion object {
        const val ACTION_ZONE_UNLOCKED = "com.example.edufocus.ACTION_ZONE_UNLOCKED"

        @Volatile
        private var instance: FocusManager? = null

        fun getInstance(): FocusManager {
            return instance ?: synchronized(this) {
                instance ?: FocusManager().also { instance = it }
            }
        }
    }
}
