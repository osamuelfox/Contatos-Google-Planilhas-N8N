package com.example.jp.ui.busca

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jp.data.local.ContatoEntity
import com.example.jp.data.repository.ContatoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel da tela de busca.
 *
 * - [contatos]: lista reativa filtrada pelo [termoBusca] com debounce de 200ms.
 * - [sincronizando]: indica se uma sync está em andamento (mostra loading na UI).
 * - [erro]: mensagem de erro efêmera exibida via Snackbar.
 * - [ultimaSync]: texto formatado da última atualização (ex.: "Atualizado: 29/07 18:40").
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class BuscaViewModel @Inject constructor(
    private val repository: ContatoRepository
) : ViewModel() {

    private val termoBusca = MutableStateFlow("")

    val contatos = termoBusca
        .debounce(200)
        .flatMapLatest { repository.buscar(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList<ContatoEntity>()
        )

    /** Contagem total — sempre reflete o banco completo, independente da busca ativa. */
    val totalContatos = repository.buscar("")
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando = _sincronizando.asStateFlow()

    private val _erro = MutableStateFlow<String?>(null)
    val erro = _erro.asStateFlow()

    val ultimaSync: String get() = repository.ultimaSyncFormatada()

    // ── Ações ────────────────────────────────────────────────────────────────

    fun onTermoMudou(novoTermo: String) {
        termoBusca.value = novoTermo
    }

    /** Chamado em LaunchedEffect(Unit) quando a tela abre. */
    fun sincronizarAoAbrir() = sincronizar(force = false)

    /** Chamado no onClick do botão "Sincronizar". */
    fun sincronizarManual() = sincronizar(force = true)

    fun limparErro() { _erro.value = null }

    private fun sincronizar(force: Boolean) {
        viewModelScope.launch {
            _sincronizando.value = true
            repository.sincronizar(force)
                .onFailure { e ->
                    _erro.value = when (e) {
                        is IOException ->
                            "Sem conexão com a internet. Mostrando dados salvos."
                        is com.squareup.moshi.JsonDataException,
                        is com.squareup.moshi.JsonEncodingException ->
                            "Erro no formato da resposta do servidor (JSON inválido). Verifique o n8n."
                        else ->
                            "Erro ao sincronizar: ${e.javaClass.simpleName}"
                    }
                }
            _sincronizando.value = false
        }
    }
}
