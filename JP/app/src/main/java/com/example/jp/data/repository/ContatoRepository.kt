package com.example.jp.data.repository

import com.example.jp.BuildConfig
import com.example.jp.data.local.ContatoDao
import com.example.jp.data.local.ContatoEntity
import com.example.jp.data.remote.ContatosApi
import com.example.jp.sync.SyncPrefs
import kotlinx.coroutines.flow.Flow
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton

private const val INTERVALO_MIN_SYNC = 15L // minutos entre sincronizações automáticas

/**
 * Fonte única da verdade do app.
 *
 * - A busca **sempre** lê do Room (resposta instantânea, funciona offline).
 * - A sincronização chama o n8n apenas quando necessário:
 *   - Automática: somente se passaram mais de [INTERVALO_MIN_SYNC] minutos.
 *   - Manual (force=true): sempre executa, sem verificar o intervalo.
 */
@Singleton
class ContatoRepository @Inject constructor(
    private val api: ContatosApi,
    private val dao: ContatoDao,
    private val prefs: SyncPrefs
) {

    // ── Busca local ──────────────────────────────────────────────────────────

    fun buscar(termo: String): Flow<List<ContatoEntity>> =
        if (termo.isBlank()) dao.listarTodos() else dao.buscar(termo.removerAcentos())

    suspend fun buscarPorId(id: String): ContatoEntity? =
        dao.buscarPorId(id)

    // ── Sincronização ────────────────────────────────────────────────────────

    /**
     * @param force Se true, ignora o intervalo mínimo e força a chamada ao n8n.
     * @return [Result.success] se sincronizou (ou pulou dentro do intervalo),
     *         [Result.failure] se houve erro de rede/parsing.
     */
    suspend fun sincronizar(force: Boolean = false): Result<Unit> {
        val minutosPassados = prefs.minutosDesdeUltimaSync()
        if (!force && minutosPassados < INTERVALO_MIN_SYNC) {
            return Result.success(Unit) // ainda dentro do intervalo — pula
        }

        return try {
            val resposta = api.getContatos(BuildConfig.N8N_API_KEY)
            val entidades = resposta.contatos.map { dto ->
                ContatoEntity(
                    id          = dto.id,
                    nome        = dto.nome,
                    telefone    = dto.telefone,
                    regiao      = dto.regiao,
                    cidade      = dto.cidade,
                    observacao  = dto.observacao,
                    origem      = dto.origem,
                    parceria    = dto.parceria,
                    statusVisita = dto.statusVisita,
                    demandas    = dto.demandas,
                    buscaNormalizada = "${dto.nome} ${dto.cidade} ${dto.regiao}".removerAcentos()
                )
            }
            dao.limpar()
            dao.inserirTodos(entidades)
            prefs.marcarSyncAgora()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Texto formatado da última sincronização (para exibição na TopBar). */
    fun ultimaSyncFormatada(): String = prefs.ultimaSyncFormatada()
}

/** Remove acentos e converte para minúsculas para permitir busca inteligente. */
fun String.removerAcentos(): String {
    val normalizado = Normalizer.normalize(this, Normalizer.Form.NFD)
    return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(normalizado, "").lowercase()
}
