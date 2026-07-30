package com.example.jp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO — Data Access Object para a tabela "contatos".
 * Todos os métodos de leitura retornam [Flow] para que a UI reaja
 * automaticamente a mudanças no banco após cada sincronização.
 */
@Dao
interface ContatoDao {

    /**
     * Busca contatos por nome, cidade ou região (case-insensitive via LIKE).
     * Suficiente para ~900 registros. Para bases maiores, migrar para FTS4.
     */
    @Query("""
        SELECT * FROM contatos
        WHERE buscaNormalizada LIKE '%' || :termoNormalizado || '%'
        ORDER BY nome COLLATE NOCASE
    """)
    fun buscar(termoNormalizado: String): Flow<List<ContatoEntity>>

    /** Retorna todos os contatos ordenados por nome (campo de busca vazio). */
    @Query("SELECT * FROM contatos ORDER BY nome COLLATE NOCASE")
    fun listarTodos(): Flow<List<ContatoEntity>>

    /** Busca um contato específico por ID (usado na tela de detalhe). */
    @Query("SELECT * FROM contatos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): ContatoEntity?

    /** Insere ou substitui todos os contatos recebidos do n8n. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(contatos: List<ContatoEntity>)

    /** Limpa toda a tabela antes de inserir os dados mais recentes. */
    @Query("DELETE FROM contatos")
    suspend fun limpar()
}
