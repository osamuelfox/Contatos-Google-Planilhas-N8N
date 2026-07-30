package com.example.jp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Room — tabela "contatos" no banco local SQLite.
 * Espelha exatamente os campos do [ContatoDto], porém sem anotações de serialização.
 */
@Entity(tableName = "contatos")
data class ContatoEntity(
    @PrimaryKey val id: String,
    val nome: String,
    val telefone: String,
    val regiao: String,
    val cidade: String,
    val observacao: String,
    val origem: String,
    val parceria: String,
    val statusVisita: String,
    val demandas: String,
    val buscaNormalizada: String
)
