package com.example.jp.data.remote

import com.example.jp.data.remote.dto.ContatosResponse
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * Interface Retrofit para o webhook do n8n.
 * Endpoint: GET https://n8n.fox.api.br/webhook/contatos
 * Autenticação: header "x-api-key" com o token configurado no local.properties.
 */
interface ContatosApi {
    @GET("webhook/contatos")
    suspend fun getContatos(
        @Header("x-api-key") apiKey: String
    ): ContatosResponse
}
