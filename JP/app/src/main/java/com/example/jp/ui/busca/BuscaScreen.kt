package com.example.jp.ui.busca

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jp.data.local.ContatoEntity

/**
 * Tela principal de busca de contatos.
 *
 * Comportamento:
 * - Ao abrir, dispara sync automática (respeita intervalo de 15 min).
 * - Campo de busca com debounce de 200 ms.
 * - Indicador de carregamento durante sync.
 * - Snackbar de erro caso a sync falhe.
 * - Botão "Sincronizar" na TopBar força atualização imediata.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscaScreen(
    onContatoClick: (String) -> Unit,
    viewModel: BuscaViewModel = hiltViewModel()
) {
    val contatos by viewModel.contatos.collectAsState()
    val totalContatos by viewModel.totalContatos.collectAsState()
    val sincronizando by viewModel.sincronizando.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val ultimaSync by viewModel.ultimaSync.collectAsState()

    var termoBusca by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Sync automática na abertura da tela
    LaunchedEffect(Unit) {
        viewModel.sincronizarAoAbrir()
    }

    // Exibe Snackbar quando há erro
    LaunchedEffect(erro) {
        erro?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.limparErro()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Meta de Votos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ultimaSync,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (sincronizando) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.sincronizarManual() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sincronizar"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Campo de busca ───────────────────────────────────────────────
            OutlinedTextField(
                value = termoBusca,
                onValueChange = { novo ->
                    termoBusca = novo
                    viewModel.onTermoMudou(novo)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por nome, cidade ou região…") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                trailingIcon = {
                    if (termoBusca.isNotEmpty()) {
                        IconButton(onClick = {
                            termoBusca = ""
                            viewModel.onTermoMudou("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // ── Contador de resultados ─────────────────────────────────────
            val textoContador = if (termoBusca.isBlank()) {
                "$totalContatos contato(s)"
            } else {
                "${contatos.size} de $totalContatos contato(s)"
            }
            Text(
                text = textoContador,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            // ── Lista de contatos ou estado vazio ───────────────────────────
            if (contatos.isEmpty() && !sincronizando) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            text = if (termoBusca.isBlank())
                                "Nenhum contato ainda.\nToque em ↻ para sincronizar."
                            else
                                "Nenhum resultado para “$termoBusca”",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = contatos,
                        key = { it.id }
                    ) { contato ->
                        ContatoItem(
                            contato = contato,
                            onClick = { onContatoClick(contato.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContatoItem(
    contato: ContatoEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar com inicial do nome (ou '?' para contatos sem nome)
            val semNome = contato.nome.isBlank()
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(22.dp),
                color = if (semNome)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = contato.nome.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (semNome)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (semNome) "(Sem nome)" else contato.nome,
                    style = if (semNome)
                        MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    else
                        MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (semNome)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(
                        contato.cidade.takeIf { it.isNotBlank() },
                        contato.regiao.takeIf { it.isNotBlank() }
                    ).joinToString(" · ").ifEmpty { "—" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (contato.telefone.isNotBlank()) {
                    Text(
                        text = contato.telefone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
