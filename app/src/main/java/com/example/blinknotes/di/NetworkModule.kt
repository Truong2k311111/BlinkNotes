package com.example.blinknotes.di

import com.example.blinknotes.data.remote.FcmApi
import com.example.blinknotes.data.remote.FirebaseAuthApi
import com.example.blinknotes.data.remote.FirebaseAuthApiImpl
import com.example.blinknotes.data.remote.FirebaseStorageApi
import com.example.blinknotes.data.remote.FirebaseStorageApiImpl
import com.example.blinknotes.data.remote.FirestoreApi
import com.example.blinknotes.data.remote.FirestoreApiImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuthApi(auth: FirebaseAuth): FirebaseAuthApi = FirebaseAuthApiImpl(auth)

    @Provides
    @Singleton
    fun provideFirestoreApi(db: FirebaseFirestore): FirestoreApi = FirestoreApiImpl(db)

    @Provides
    @Singleton
    fun provideFirebaseStorageApi(storage: FirebaseStorage): FirebaseStorageApi = FirebaseStorageApiImpl(storage)

    @Provides
    @Singleton
    fun provideFcmApi(): FcmApi {
        return Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FcmApi::class.java)
    }
}