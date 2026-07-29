package com.example.jp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jp.ui.busca.BuscaScreen
import com.example.jp.ui.detalhe.DetalheContatoScreen
import com.example.jp.ui.theme.JpTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Única Activity do app.
 * - @AndroidEntryPoint habilita injeção de dependência via Hilt.
 * - Configura o NavHost com duas rotas: "busca" e "detalhe/{id}".
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "busca"
                    ) {
                        composable("busca") {
                            BuscaScreen(
                                onContatoClick = { id ->
                                    // URI-encode evita crash com IDs que têm acentos/espaços
                                    val idEncoded = Uri.encode(id)
                                    navController.navigate("detalhe/$idEncoded")
                                }
                            )
                        }
                        composable("detalhe/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id") ?: ""
                            DetalheContatoScreen(
                                contatoId = id,
                                onVoltar = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
