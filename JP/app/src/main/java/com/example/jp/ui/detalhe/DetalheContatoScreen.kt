package com.example.jp.ui.detalhe

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jp.data.local.ContatoEntity

/**
 * Tela de detalhe de um contato.
 *
 * - Carrega o contato pelo ID direto do Room (sem nova chamada de rede).
 * - Exibe todos os campos da planilha.
 * - Botão "Salvar na Agenda" abre o app nativo de Contatos pré-preenchido.
 *   Fica desabilitado quando o telefone está em branco.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalheContatoScreen(
    contatoId: String,
    onVoltar: () -> Unit,
    viewModel: DetalheViewModel = hiltViewModel()
) {
    val contato by viewModel.contato.collectAsState()
    val carregado by viewModel.carregado.collectAsState()

    LaunchedEffect(contatoId) {
        viewModel.carregarContato(contatoId)
    }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contato?.nome ?: "Carregando…") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            contato?.let { c ->
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = { salvarNaAgenda(context, c) },
                        enabled = c.telefone.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (c.telefone.isBlank()) "Sem telefone cadastrado"
                                   else "Salvar na Agenda",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            !carregado -> {
                // Ainda carregando do Room
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            contato == null -> {
                // Contato não encontrado no banco
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Contato não encontrado.\nSincronize e tente novamente.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(onClick = onVoltar) { Text("Voltar") }
                    }
                }
            }
            else -> {
                val c = contato!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CampoDetalhe(label = "Nome",          valor = c.nome)
                    CampoDetalhe(label = "Cidade",        valor = c.cidade)
                    CampoDetalhe(label = "Região",        valor = c.regiao)
                    CampoDetalhe(label = "Telefone",      valor = c.telefone.ifBlank { "—" })
                    CampoDetalhe(label = "Observação",    valor = c.observacao.ifBlank { "—" })
                    CampoDetalhe(label = "Origem",        valor = c.origem.ifBlank { "—" })
                    CampoDetalhe(label = "Parceria",      valor = c.parceria.ifBlank { "—" })
                    CampoDetalhe(label = "Status Visita", valor = c.statusVisita.ifBlank { "—" })
                    CampoDetalhe(label = "Demandas",      valor = c.demandas.ifBlank { "—" })
                }
            }
        }
    }
}

@Composable
private fun CampoDetalhe(label: String, valor: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Abre o app nativo de Contatos com os dados pré-preenchidos.
 * Não requer permissão WRITE_CONTACTS — o usuário confirma no app de Contatos.
 */
private fun salvarNaAgenda(context: Context, contato: ContatoEntity) {
    val notas = listOfNotNull(
        contato.cidade.takeIf     { it.isNotBlank() }?.let { "Cidade: $it" },
        contato.regiao.takeIf     { it.isNotBlank() }?.let { "Região: $it" },
        contato.observacao.takeIf { it.isNotBlank() }
    ).joinToString("\n")

    val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
        type = ContactsContract.RawContacts.CONTENT_TYPE
        putExtra(ContactsContract.Intents.Insert.NAME,  contato.nome)
        putExtra(ContactsContract.Intents.Insert.PHONE, contato.telefone)
        if (notas.isNotBlank()) {
            putExtra(ContactsContract.Intents.Insert.NOTES, notas)
        }
    }
    context.startActivity(intent)
}
