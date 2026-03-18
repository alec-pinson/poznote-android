package com.poznote.android.di

import com.poznote.android.data.local.AuthPreferences
import com.poznote.android.data.remote.api.PoznoteApi
import com.poznote.android.data.remote.interceptor.BasicAuthInterceptor
import com.poznote.android.data.remote.interceptor.UrlOverrideInterceptor
import com.poznote.android.data.remote.interceptor.UserIdInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        basicAuthInterceptor: BasicAuthInterceptor,
        userIdInterceptor: UserIdInterceptor,
        urlOverrideInterceptor: UrlOverrideInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(urlOverrideInterceptor)
            .addInterceptor(basicAuthInterceptor)
            .addInterceptor(userIdInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://placeholder.local/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun providePoznoteApi(retrofit: Retrofit): PoznoteApi =
        retrofit.create(PoznoteApi::class.java)
}
