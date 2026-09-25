package com.example.bouldertrack.domain.manager

import android.content.Context
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Singleton zarządzający stoperem odpoczynku pomiędzy wstawkami na boulderach.
 *
 * Udostępnia reaktywne strumienie [StateFlow] reprezentujące pozostały czas oraz stan aktywności stopera,
 * co pozwala na zsynchronizowaną prezentację na różnych ekranach aplikacji.
 * Po zakończeniu odliczania (0s) uruchamia sygnał dźwiękowy oraz 3-impulsową wibrację urządzenia.
 */
object TimerManager {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null
    private var appContext: Context? = null
    private var activeRingtone: Ringtone? = null

    private val _timeRemaining = MutableStateFlow(180)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private val _targetTime = MutableStateFlow(180)
    val targetTime: StateFlow<Int> = _targetTime.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    /**
     * Inicjalizuje kontekst aplikacji niezbędny do wywoływania wibracji i odtwarzania dźwięków dzwonka.
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Uruchamia lub wznawia odliczanie czasu w korutynie na głównym wątku ([Dispatchers.Main]).
     * Jeśli stoper wcześniej dobiegł końca (0s), automatycznie resetuje go do ustawionego czasu docelowego.
     */
    fun startTimer() {
        if (_isRunning.value) return
        stopAlertSound()
        if (_timeRemaining.value <= 0) {
            _timeRemaining.value = _targetTime.value
        }
        _isRunning.value = true
        startServiceNotification()
        timerJob = scope.launch {
            while (_timeRemaining.value > 0) {
                delay(1000L)
                _timeRemaining.value -= 1
            }
            _isRunning.value = false
            notifyTimerFinished()
        }
    }

    /**
     * Pauzuje bieżące odliczanie i anuluje działającą korutynę.
     */
    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        stopAlertSound()
        updateServiceNotification()
    }

    /**
     * Przełącza stan stopera między odliczaniem a pauzą.
     */
    fun toggleTimer() {
        if (_isRunning.value) pauseTimer() else startTimer()
    }

    /**
     * Zatrzymuje stoper i przywraca pozostały czas do pierwotnej wartości docelowej.
     */
    fun resetTimer() {
        pauseTimer()
        _timeRemaining.value = _targetTime.value
        stopAlertSound()
        stopServiceNotification()
    }

    /**
     * Modyfikuje bieżące odliczanie i czas docelowy o zadaną liczbę [seconds] (np. +30s lub -30s).
     * Minimalny czas docelowy jest ograniczony do 30 sekund.
     */
    fun addTime(seconds: Int) {
        stopAlertSound()
        _targetTime.value = (_targetTime.value + seconds).coerceAtLeast(30)
        if (!_isRunning.value && _timeRemaining.value == _targetTime.value - seconds) {
            _timeRemaining.value = _targetTime.value
        } else {
            _timeRemaining.value = (_timeRemaining.value + seconds).coerceAtLeast(0)
        }
    }

    /**
     * Ustawia domyślny czas odpoczynku z preferencji i aktualizuje bieżący stoper, jeśli nie jest uruchomiony.
     */
    fun setDefaultTime(seconds: Int) {
        _targetTime.value = seconds.coerceAtLeast(10)
        if (!_isRunning.value) {
            _timeRemaining.value = _targetTime.value
        }
    }

    /**
     * Wywołuje wibrację oraz sygnał dźwiękowy zawiadamiający wspinacza o upływie czasu odpoczynku.
     */
    private fun notifyTimerFinished() {
        val context = appContext ?: return
        triggerVibration(context)
        playAlertSound(context)
    }

    /**
     * Wyzwala trzykrotny, wyraźny impuls wibracji sygnalizujący gotowość na kolejną wstawkę.
     */
    private fun triggerVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Wzorzec wibracji: czekaj 0ms, wibruj 350ms, pauza 150ms, wibruj 350ms, pauza 150ms, wibruj 450ms
                    val timings = longArrayOf(0, 350, 150, 350, 150, 450)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 350, 150, 350, 150, 450), -1)
                }
            }
        } catch (_: Exception) {
            // Ignorujemy błędy braku wsparcia wibracji na urządzeniu
        }
    }

    /**
     * Odtwarza domyślny sygnał powiadomienia lub alarmu z automatycznym wyłączeniem po 3.5 sekundy.
     */
    private fun playAlertSound(context: Context) {
        try {
            stopAlertSound()
            val soundUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            if (soundUri != null) {
                val ringtone = RingtoneManager.getRingtone(context, soundUri)
                if (ringtone != null) {
                    activeRingtone = ringtone
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ringtone.isLooping = false
                    }
                    ringtone.play()

                    // Bezpieczne zatrzymanie dzwonka po krótkim czasie w przypadku zapętlonych dźwięków alarmu
                    scope.launch {
                        delay(3500L)
                        if (activeRingtone == ringtone && ringtone.isPlaying) {
                            ringtone.stop()
                            activeRingtone = null
                        }
                    }
                    return
                }
            }
            // Zapasowy generator tonów systemowych
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 800)
        } catch (_: Exception) {
            // Bezpieczna obsługa środowisk bez wyjścia audio (np. emulator w trybie cichym)
        }
    }

    /**
     * Zatrzymuje aktualnie grający sygnał dźwiękowy.
     */
    fun stopAlertSound() {
        try {
            activeRingtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
            activeRingtone = null
        } catch (_: Exception) {
            // Ignorujemy ewentualne wyjątki podczas zwalniania zasobów audio
        }
    }

    private fun startServiceNotification() {
        val context = appContext ?: return
        try {
            val intent = android.content.Intent(context, com.example.bouldertrack.service.RestTimerService::class.java).apply {
                action = com.example.bouldertrack.service.RestTimerService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (_: Exception) {}
    }

    private fun updateServiceNotification() {
        val context = appContext ?: return
        try {
            val intent = android.content.Intent(context, com.example.bouldertrack.service.RestTimerService::class.java).apply {
                action = com.example.bouldertrack.service.RestTimerService.ACTION_UPDATE
            }
            context.startService(intent)
        } catch (_: Exception) {}
    }

    private fun stopServiceNotification() {
        val context = appContext ?: return
        try {
            val intent = android.content.Intent(context, com.example.bouldertrack.service.RestTimerService::class.java).apply {
                action = com.example.bouldertrack.service.RestTimerService.ACTION_STOP
            }
            context.startService(intent)
        } catch (_: Exception) {}
    }
}
