package com.example.blinknotes.di

import com.example.blinknotes.data.remote.FcmApi
import com.example.blinknotes.data.repository.ChatRepositoryImpl
import com.example.blinknotes.domain.repository.ChatRepository
import com.example.blinknotes.domain.usecase.chat.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        fcmApi: FcmApi
    ): ChatRepository {
        return ChatRepositoryImpl(firestore, auth, fcmApi)
    }

    @Provides
    @Singleton
    fun provideSendMessageUseCase(repository: ChatRepository): SendMessageUseCase {
        return SendMessageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetMessagesUseCase(repository: ChatRepository): GetMessagesUseCase {
        return GetMessagesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMarkMessagesAsReadUseCase(repository: ChatRepository): MarkMessagesAsReadUseCase {
        return MarkMessagesAsReadUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetUnreadMessageCountUseCase(repository: ChatRepository): GetUnreadMessageCountUseCase {
        return GetUnreadMessageCountUseCase(repository)
    }
} 