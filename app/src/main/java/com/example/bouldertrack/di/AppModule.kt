package com.example.bouldertrack.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.bouldertrack.data.repository.BoulderRepositoryImpl
import com.example.bouldertrack.data.repository.GoalRepositoryImpl
import com.example.bouldertrack.data.repository.HangboardRepositoryImpl
import com.example.bouldertrack.data.repository.PreferencesRepositoryImpl
import com.example.bouldertrack.data.repository.SessionRepositoryImpl
import com.example.bouldertrack.database.AppDatabase
import com.example.bouldertrack.domain.repository.BoulderRepository
import com.example.bouldertrack.domain.repository.GoalRepository
import com.example.bouldertrack.domain.repository.HangboardRepository
import com.example.bouldertrack.domain.repository.PreferencesRepository
import com.example.bouldertrack.domain.repository.SessionRepository
import com.example.bouldertrack.presentation.betalibrary.BetaLibraryViewModel
import com.example.bouldertrack.presentation.sessiondetail.SessionDetailViewModel
import com.example.bouldertrack.presentation.sessionlist.SessionListViewModel
import com.example.bouldertrack.presentation.settings.SettingsViewModel
import com.example.bouldertrack.presentation.stats.StatsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Główny moduł wstrzykiwania zależności (Koin DI).
 * Konfiguruje sterownik bazy danych, instancję bazy, repozytoria oraz modele ViewModel.
 */
val appModule = module {
    // Warstwa danych / baza SQLDelight
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = androidContext(),
            name = "boulder_track.db"
        )
    }
    single { AppDatabase(get()) }

    // Repozytoria
    single<SessionRepository> { SessionRepositoryImpl(get()) }
    single<BoulderRepository> { BoulderRepositoryImpl(get()) }
    single<PreferencesRepository> { PreferencesRepositoryImpl(androidContext()) }
    single<GoalRepository> { GoalRepositoryImpl(get()) }
    single<HangboardRepository> { HangboardRepositoryImpl(get()) }

    // Modele ViewModel warstwy prezentacji
    viewModel { SessionListViewModel(get(), get()) }
    viewModel { SessionDetailViewModel(get(), get(), get()) }
    viewModel { StatsViewModel(get(), get()) }
    viewModel { BetaLibraryViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { com.example.bouldertrack.presentation.projects.ProjectsViewModel(get()) }
}