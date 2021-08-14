package dev.ahmedmourad.githubsurfer.application

import androidx.multidex.MultiDexApplication
import dev.ahmedmourad.githubsurfer.di.ApplicationComponent
import dev.ahmedmourad.githubsurfer.di.ContextModule
import dev.ahmedmourad.githubsurfer.di.DaggerApplicationComponent
import dev.ahmedmourad.githubsurfer.di.DaggerComponentProvider
import timber.log.Timber

@Suppress("unused")
internal class SurferApplication : MultiDexApplication(), DaggerComponentProvider {
    override val component: ApplicationComponent = DaggerApplicationComponent.builder()
        .contextModule(ContextModule(this))
        .build()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
