package com.example.jp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class raiz — obrigatório para Hilt funcionar.
 * Referenciado no AndroidManifest via android:name=".JpApplication".
 */
@HiltAndroidApp
class JpApplication : Application()
