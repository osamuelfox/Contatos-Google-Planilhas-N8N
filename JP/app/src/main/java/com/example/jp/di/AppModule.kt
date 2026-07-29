package com.example.jp.di

import android.content.Context
import androidx.room.Room
import com.example.jp.BuildConfig
import com.example.jp.data.local.AppDatabase
import com.example.jp.data.local.ContatoDao
import com.example.jp.data.remote.ContatosApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * Módulo Hilt que provê todas as dependências singleton do app:
 * - Moshi (serialização JSON)
 * - Retrofit + ContatosApi (camada remota)
 * - AppDatabase + ContatoDao (camada local)
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.N8N_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideContatosApi(retrofit: Retrofit): ContatosApi =
        retrofit.create(ContatosApi::class.java)

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "jp_database"
    ).build()

    @Provides
    @Singleton
    fun provideContatoDao(database: AppDatabase): ContatoDao =
        database.contatoDao()
}
