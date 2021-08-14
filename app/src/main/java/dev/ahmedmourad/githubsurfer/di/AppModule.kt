package dev.ahmedmourad.githubsurfer.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dev.ahmedmourad.githubsurfer.common.AssistedViewModelFactory
import dev.ahmedmourad.githubsurfer.common.MainViewModel
import dev.ahmedmourad.githubsurfer.users.profile.UserProfileViewModel
import dev.ahmedmourad.githubsurfer.users.users.FindUsersViewModel
import dev.ahmedmourad.githubsurfer.users.search.SearchResultsViewModel

@Module(includes = [ContextModule::class, AppBindingsModule::class])
interface AppModule

@Module
interface AppBindingsModule {

    @Binds
    fun bindMainViewModel(
        impl: MainViewModel.Factory
    ): AssistedViewModelFactory<MainViewModel>

    @Binds
    fun bindFindUsersViewModel(
        impl: FindUsersViewModel.Factory
    ): AssistedViewModelFactory<FindUsersViewModel>

    @Binds
    fun bindSearchResultsViewModel(
        impl: SearchResultsViewModel.Factory
    ): AssistedViewModelFactory<SearchResultsViewModel>

    @Binds
    fun bindUserProfileViewModel(
        impl: UserProfileViewModel.Factory
    ): AssistedViewModelFactory<UserProfileViewModel>
}

@Module
class ContextModule(private val appCtx: Context) {
    @Provides
    fun provideAppContext(): Context {
        return appCtx
    }
}
