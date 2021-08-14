package dev.ahmedmourad.githubsurfer.di

import dagger.Component
import dev.ahmedmourad.githubsurfer.common.MainActivity
import dev.ahmedmourad.githubsurfer.core.di.CoreModule
import dev.ahmedmourad.githubsurfer.users.di.UsersModule
import dev.ahmedmourad.githubsurfer.users.users.FindUsersFragment
import dev.ahmedmourad.githubsurfer.users.search.SearchResultsFragment
import dev.ahmedmourad.githubsurfer.users.profile.UserProfileFragment
import javax.inject.Singleton

@Component(modules = [
    CoreModule::class,
    UsersModule::class,
    AppModule::class
])
@Singleton
internal interface ApplicationComponent {
    fun inject(target: MainActivity)
    fun inject(target: FindUsersFragment)
    fun inject(target: SearchResultsFragment)
    fun inject(target: UserProfileFragment)
}
