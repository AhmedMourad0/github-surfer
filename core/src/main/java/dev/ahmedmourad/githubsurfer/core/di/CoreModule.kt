package dev.ahmedmourad.githubsurfer.core.di

import dev.ahmedmourad.githubsurfer.core.users.di.UsersBindingsModule
import dagger.Module

@Module(includes = [
    UsersBindingsModule::class
])
interface CoreModule
