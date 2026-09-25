package com.example.bouldertrack.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.bouldertrack.MainActivity
import com.example.bouldertrack.R
import com.example.bouldertrack.domain.manager.TimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Usługa pierwszoplanowa (Foreground Service) utrzymująca działanie i powiadomienie
 * stopera odpoczynku w tle, nawet gdy aplikacja zostanie zminimalizowana lub ekran wygaszony.
 */
class RestTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var observerJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()
        observeTimer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE -> TimerManager.toggleTimer()
            ACTION_ADD_30 -> TimerManager.addTime(30)
            ACTION_RESET -> {
                TimerManager.resetTimer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_STOP -> {
                TimerManager.pauseTimer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val notification = buildRunningNotification(
            timeRemaining = TimerManager.timeRemaining.value,
            isRunning = TimerManager.isRunning.value
        )
        startForeground(NOTIFICATION_ID_RUNNING, notification)

        return START_STICKY
    }

    private fun observeTimer() {
        observerJob?.cancel()
        observerJob = serviceScope.launch {
            combine(
                TimerManager.timeRemaining,
                TimerManager.isRunning
            ) { remaining, running ->
                remaining to running
            }.collect { (remaining, running) ->
                if (remaining <= 0 && !running) {
                    showFinishedAlertNotification()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    val notification = buildRunningNotification(remaining, running)
                    notificationManager.notify(NOTIFICATION_ID_RUNNING, notification)
                }
            }
        }
    }

    private fun buildRunningNotification(timeRemaining: Int, isRunning: Boolean): Notification {
        val minutes = timeRemaining / 60
        val seconds = timeRemaining % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Akcja Toggle (Pauza / Wznów)
        val toggleIntent = Intent(this, RestTimerService::class.java).apply { action = ACTION_TOGGLE }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val toggleTitle = if (isRunning) getString(R.string.stoper_pauza) else getString(R.string.stoper_wznow)

        // Akcja +30s
        val addTimeIntent = Intent(this, RestTimerService::class.java).apply { action = ACTION_ADD_30 }
        val addTimePendingIntent = PendingIntent.getService(
            this,
            2,
            addTimeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Akcja Stop / Reset
        val resetIntent = Intent(this, RestTimerService::class.java).apply { action = ACTION_RESET }
        val resetPendingIntent = PendingIntent.getService(
            this,
            3,
            resetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID_TIMER)
            .setContentTitle(getString(R.string.stoper_powiadomienie_tytul))
            .setContentText(getString(R.string.stoper_powiadomienie_odliczanie, timeFormatted))
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(isRunning)
            .setOnlyAlertOnce(true)
            .addAction(0, toggleTitle, togglePendingIntent)
            .addAction(0, getString(R.string.stoper_plus_30), addTimePendingIntent)
            .addAction(0, getString(R.string.stoper_reset), resetPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun showFinishedAlertNotification() {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alertNotification = NotificationCompat.Builder(this, CHANNEL_ID_ALERT)
            .setContentTitle(getString(R.string.stoper_zakonczony_tytul))
            .setContentText(getString(R.string.stoper_zakonczony_tekst))
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(NOTIFICATION_ID_ALERT, alertNotification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Kanał dla cichego odliczania w toku
            val runningChannel = NotificationChannel(
                CHANNEL_ID_TIMER,
                getString(R.string.stoper_powiadomienie_tytul),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Bieżące odliczanie czasu odpoczynku"
                setShowBadge(false)
            }

            // Kanał dla alarmu o zakończeniu odpoczynku
            val alertChannel = NotificationChannel(
                CHANNEL_ID_ALERT,
                getString(R.string.stoper_zakonczony_tytul),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Powiadomienie o zakończeniu czasu odpoczynku"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(runningChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    override fun onDestroy() {
        observerJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID_TIMER = "rest_timer_running_channel"
        const val CHANNEL_ID_ALERT = "rest_timer_alert_channel"
        const val NOTIFICATION_ID_RUNNING = 1001
        const val NOTIFICATION_ID_ALERT = 1002

        const val ACTION_START = "com.example.bouldertrack.ACTION_START"
        const val ACTION_UPDATE = "com.example.bouldertrack.ACTION_UPDATE"
        const val ACTION_TOGGLE = "com.example.bouldertrack.ACTION_TOGGLE"
        const val ACTION_ADD_30 = "com.example.bouldertrack.ACTION_ADD_30"
        const val ACTION_RESET = "com.example.bouldertrack.ACTION_RESET"
        const val ACTION_STOP = "com.example.bouldertrack.ACTION_STOP"
    }
}
