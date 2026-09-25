package com.example.bouldertrack

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.example.bouldertrack.di.appModule
import com.example.bouldertrack.domain.manager.TimerManager
import com.example.bouldertrack.domain.repository.PreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

/**
 * Główna klasa aplikacji [Application].
 * Inicjalizuje wstrzykiwanie zależności Koin, konfiguruje domyślny czas stopera z preferencji
 * oraz rejestruje dekoder klatek wideo w bibliotece Coil do generowania miniatur patentów.
 */
class BoulderTrackApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        // Inicjalizacja kontenera DI Koin
        startKoin {
            androidContext(this@BoulderTrackApp)
            modules(appModule)
        }

        // Inicjalizacja stopera odpoczynku (kontekst dla powiadomień dźwiękowych i wibracji)
        TimerManager.init(this)

        // Inicjalizacja domyślnego czasu stopera odpoczynku na podstawie preferencji użytkownika
        val preferencesRepository: PreferencesRepository = GlobalContext.get().get()
        TimerManager.setDefaultTime(preferencesRepository.getDefaultRestTime())
    }

    /**
     * Konfiguruje fabrykę [ImageLoader] z obsługą [VideoFrameDecoder],
     * co umożliwia renderowanie miniatur zarejestrowanych filmów wideo w aplikacji.
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}