package com.yilmaz.ledcontroller.features.led_control.di

import android.content.Context
import com.yilmaz.ledcontroller.features.led_control.data.LedControllerImpl
import com.yilmaz.ledcontroller.features.led_control.domain.LedController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LedControllerModule {

    @Provides
    @Singleton
    fun provideLedController(
        @ApplicationContext context: Context
    ): LedController = LedControllerImpl(context)

}