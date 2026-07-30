package com.example.jp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Banco de dados Room do app.
 * - version = 1 (incrementar + Migration ao adicionar colunas futuramente)
 * - exportSchema = false por ora (em produção, setar true e versionar o schema)
 *
 * O Singleton é criado e injetado via Hilt em [com.example.jp.di.AppModule].
 */
@Database(
    entities = [ContatoEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contatoDao(): ContatoDao
}
