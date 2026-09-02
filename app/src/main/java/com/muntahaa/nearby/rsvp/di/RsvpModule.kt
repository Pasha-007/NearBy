package com.muntahaa.nearby.rsvp.di

import com.muntahaa.nearby.rsvp.RsvpRepository
import com.muntahaa.nearby.rsvp.RsvpRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RsvpBindingsModule {
    @Binds
    @Singleton
    abstract fun bindRsvpRepository(impl: RsvpRepositoryImpl): RsvpRepository
}
