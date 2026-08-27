package com.muntahaa.nearby.events.di

import com.google.firebase.firestore.FirebaseFirestore
import com.muntahaa.nearby.events.EventRepository
import com.muntahaa.nearby.events.EventRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class EventBindingsModule {
    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository
}
