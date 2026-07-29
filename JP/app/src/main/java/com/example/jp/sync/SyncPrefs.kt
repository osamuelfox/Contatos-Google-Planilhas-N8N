package com.example.jp.sync

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME  = "sync_prefs"
private const val KEY_LAST_SYNC = "last_sync_ms"

/**
 * Gerencia o timestamp da última sincronização bem-sucedida.
 * Persistido em SharedPreferences — sobrevive a reinicializações do app.
 */
@Singleton
class SyncPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Retorna quantos minutos se passaram desde a última sync (0 se nunca sincronizou). */
    fun minutosDesdeUltimaSync(): Long {
        val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L)
        if (lastSync == 0L) return Long.MAX_VALUE
        return (System.currentTimeMillis() - lastSync) / 60_000
    }

    /** Marca o momento atual como última sync bem-sucedida. */
    fun marcarSyncAgora() {
        prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply()
    }

    /** Retorna o timestamp da última sync formatado (para exibição na UI). */
    fun ultimaSyncFormatada(): String {
        val ms = prefs.getLong(KEY_LAST_SYNC, 0L)
        if (ms == 0L) return "Nunca sincronizado"
        val sdf = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale("pt", "BR"))
        return "Atualizado: ${sdf.format(java.util.Date(ms))}"
    }
}
