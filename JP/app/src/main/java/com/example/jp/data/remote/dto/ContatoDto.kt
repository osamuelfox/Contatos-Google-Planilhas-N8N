package com.example.jp.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Envelope da resposta do webhook n8n.
 * Exemplo de JSON:
 * {
 *   "atualizadoEm": "2026-07-29T21:40:05.324Z",
 *   "contatos": [ ... ]
 * }
 */
@JsonClass(generateAdapter = true)
data class ContatosResponse(
    @Json(name = "atualizadoEm") val atualizadoEm: String,
    @Json(name = "contatos")     val contatos: List<ContatoDto>
)

/**
 * Representa um contato normalizado pelo node "Code" do n8n.
 * Todos os campos String — campos vazios chegam como "" (nunca null).
 */
@JsonClass(generateAdapter = true)
data class ContatoDto(
    @Json(name = "id")           val id: String,
    @Json(name = "nome")         val nome: String,
    @Json(name = "telefone")     val telefone: String,
    @Json(name = "regiao")       val regiao: String,
    @Json(name = "cidade")       val cidade: String,
    @Json(name = "observacao")   val observacao: String,
    @Json(name = "origem")       val origem: String,
    @Json(name = "parceria")     val parceria: String,
    @Json(name = "statusVisita") val statusVisita: String,
    @Json(name = "demandas")     val demandas: String
)
