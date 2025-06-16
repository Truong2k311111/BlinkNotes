package com.example.blinknotes.di

import android.content.Context
import android.content.SharedPreferences
import com.example.blinknotes.domain.repository.AuthRepository
import com.example.blinknotes.domain.repository.UserRepository
import com.example.blinknotes.domain.repository.PostRepository
import com.example.blinknotes.domain.usecase.auth.LoginUseCase
import com.example.blinknotes.domain.usecase.auth.RegisterUseCase
import com.example.blinknotes.domain.usecase.auth.ResetPasswordUseCase
import com.example.blinknotes.domain.usecase.auth.SignInWithGoogleUseCase
import com.example.blinknotes.domain.usecase.auth.UpdateUserProfileUseCase
import com.example.blinknotes.domain.usecase.user.GetUserProfileUseCase
import com.example.blinknotes.domain.usecase.user.GetCurrentUserUseCase
import com.example.blinknotes.domain.usecase.user.UpdateUserUseCase
import com.example.blinknotes.domain.usecase.user.GetUserByIdUseCase
import com.example.blinknotes.domain.usecase.user.GetAllUsersUseCase
import com.example.blinknotes.domain.usecase.post.GetPostsByUserIdsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("blinknotes_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase {
        return LoginUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideRegisterUseCase(authRepository: AuthRepository): RegisterUseCase {
        return RegisterUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideResetPasswordUseCase(authRepository: AuthRepository): ResetPasswordUseCase {
        return ResetPasswordUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateUserProfileUseCase(authRepository: AuthRepository): UpdateUserProfileUseCase {
        return UpdateUserProfileUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserProfileUseCase(userRepository: UserRepository): GetUserProfileUseCase {
        return GetUserProfileUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideSignInWithGoogleUseCase(authRepository: AuthRepository): SignInWithGoogleUseCase {
        return SignInWithGoogleUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideGetCurrentUserUseCase(userRepository: UserRepository): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateUserUseCase(userRepository: UserRepository): UpdateUserUseCase {
        return UpdateUserUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserByIdUseCase(userRepository: UserRepository): GetUserByIdUseCase {
        return GetUserByIdUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetAllUsersUseCase(userRepository: UserRepository): GetAllUsersUseCase {
        return GetAllUsersUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetPostsByUserIdsUseCase(postRepository: PostRepository): GetPostsByUserIdsUseCase {
        return GetPostsByUserIdsUseCase(postRepository)
    }
}