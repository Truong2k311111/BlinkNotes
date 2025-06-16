package com.example.blinknotes.di

import com.example.blinknotes.data.repository.AuthRepositoryImpl
import com.example.blinknotes.data.repository.PostRepositoryImpl
import com.example.blinknotes.data.repository.UserRepositoryImpl
import com.example.blinknotes.data.repository.NotificationRepositoryImpl
import com.example.blinknotes.data.repository.CommentRepositoryImpl
import com.example.blinknotes.data.repository.SystemNotificationRepositoryImpl
import com.example.blinknotes.domain.repository.AuthRepository
import com.example.blinknotes.domain.repository.PostRepository
import com.example.blinknotes.domain.repository.UserRepository
import com.example.blinknotes.domain.repository.NotificationRepository
import com.example.blinknotes.domain.repository.CommentRepository
import com.example.blinknotes.domain.repository.SystemNotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    @Binds
    @Singleton // Scope the binding to the application's lifetime
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton // Scope the binding to the application's lifetime
    abstract fun bindCommentRepository(
        commentRepositoryImpl: CommentRepositoryImpl
    ): CommentRepository

    @Binds
    @Singleton // Scope the binding to the application's lifetime
    abstract fun bindSystemNotificationRepository(
        systemNotificationRepositoryImpl: SystemNotificationRepositoryImpl
    ): SystemNotificationRepository
} 