package dev.ahmedmourad.githubsurfer.users.preferences.di

import dagger.Binds
import dagger.Module
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.users.preferences.PreferencesManagerImpl
import dev.ahmedmourad.githubsurfer.users.repo.PreferencesManager

@Module
internal interface PreferencesBindingsModule {
    @Binds
    @Reusable
    fun bindPreferencesManager(
        impl: PreferencesManagerImpl
    ): PreferencesManager
}
