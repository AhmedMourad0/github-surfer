package dev.ahmedmourad.githubsurfer.core.users.di

import dagger.Binds
import dagger.Module
import dagger.Reusable
import dev.ahmedmourad.githubsurfer.core.users.usecases.*
import dev.ahmedmourad.githubsurfer.core.users.usecases.FindAllUsersImpl

@Module
internal interface UsersBindingsModule {

    @Binds
    @Reusable
    fun bindFindAllPosts(
        impl: FindAllUsersImpl
    ): FindAllUsers

    @Binds
    @Reusable
    fun bindFindUsersBy(
        impl: FindUsersByImpl
    ): FindUsersBy

    @Binds
    @Reusable
    fun bindFindUserDetails(
        impl: FindUserDetailsImpl
    ): FindUserDetails

    @Binds
    @Reusable
    fun bindUpdateNotes(
        impl: UpdateNotesImpl
    ): UpdateNotes
}
