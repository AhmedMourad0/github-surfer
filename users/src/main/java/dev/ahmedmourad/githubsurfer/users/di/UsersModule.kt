package dev.ahmedmourad.githubsurfer.users.di

import dagger.Binds
import dagger.Module
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.core.users.UsersRepository
import dev.ahmedmourad.githubsurfer.users.local.di.LocalBindingsModule
import dev.ahmedmourad.githubsurfer.users.local.di.LocalProvidersModule
import dev.ahmedmourad.githubsurfer.users.preferences.di.PreferencesBindingsModule
import dev.ahmedmourad.githubsurfer.users.remote.di.RemoteBindingsModule
import dev.ahmedmourad.githubsurfer.users.remote.di.RemoteProvidersModule
import dev.ahmedmourad.githubsurfer.users.repo.UsersRepositoryImpl

@Module(includes = [
    LocalBindingsModule::class,
    LocalProvidersModule::class,
    RemoteBindingsModule::class,
    RemoteProvidersModule::class,
    PreferencesBindingsModule::class,
    UsersBindingsModule::class
])
interface UsersModule

@Module
internal interface UsersBindingsModule {
    @Binds
    @Reusable
    fun bindUsersRepository(
        impl: UsersRepositoryImpl
    ): UsersRepository
}
