package com.example.jp.ui.detalhe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jp.data.local.ContatoEntity
import com.example.jp.data.repository.ContatoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel da tela de detalhe.
 *
 * Estados:
 * - [carregado] = false → ainda consultando o Room (mostra loading)
 * - [carregado] = true + [contato] = null → ID não encontrado no banco
 * - [carregado] = true + [contato] != null → exibe os dados normalmente
 */
@HiltViewModel
class DetalheViewModel @Inject constructor(
    private val repository: ContatoRepository
) : ViewModel() {

    private val _contato = MutableStateFlow<ContatoEntity?>(null)
    val contato = _contato.asStateFlow()

    private val _carregado = MutableStateFlow(false)
    val carregado = _carregado.asStateFlow()

    fun carregarContato(id: String) {
        _carregado.value = false
        viewModelScope.launch {
            _contato.value = repository.buscarPorId(id)
            _carregado.value = true
        }
    }
}
