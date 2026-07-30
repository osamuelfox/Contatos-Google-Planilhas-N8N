# App de Busca de Contatos — Plano de Implementação por Fases

> **Stack**: Android (Kotlin + Jetpack Compose) · Room · Retrofit · Hilt · n8n · Google Sheets  
> **Última atualização**: 2026-07-29 19:32

---

## Status geral

| Fase | Descrição | Status |
|------|-----------|--------|
| **0 — Preparação** | Ambiente, acessos, projeto Android criado | ✅ Concluída |
| **1 — n8n** | Workflow testado e validado com curl | ✅ Concluída |
| **2 — Estrutura Android** | Kotlin + Compose + Room + Retrofit + Hilt | ✅ Concluída |
| **3 — Correções de runtime** | Bugs de navegação, estado vazio e detalhe | ✅ Concluída |
| **4 — UI Compose** | Telas de busca e detalhe (implementadas na Fase 2) | ✅ Concluída |
| **5 — Validação no emulador** | Guia de testes e refinamentos finais | ✅ Concluída |
| **6 — Extras** | WorkManager, FTS4, filtros | ⏳ Futuro |

---

## ✅ Fase 0 — Preparação (Concluída pelo usuário)

- Conta n8n Cloud ativa com HTTPS (`https://n8n.fox.api.br`)
- OAuth2 Google configurado no n8n
- Planilha `Meta de Votos` compartilhada e acessível
- Projeto Android criado no Android Studio (AGP 9.3.1, `minSdk 26`, `targetSdk 36`)
- `local.properties` já estava no `.gitignore`

---

## ✅ Fase 1 — Backend n8n (Concluída pelo usuário)

### Workflow validado

```
Webhook (GET /contatos, x-api-key)
    └── Google Sheets (Read rows — Meta de Votos)
         └── Code JS (filtro + normalização de campos)
              └── Code JS (agrega em envelope {atualizadoEm, contatos})
                   └── Respond to Webhook
```

### Endpoint em produção
- **URL**: `https://n8n.fox.api.br/webhook/contatos`
- **Auth**: header `x-api-key`
- **Resposta**: `{ "atualizadoEm": "...", "contatos": [...] }`

### Validações confirmadas
- [x] Autenticação por header ativa (sem o header → 401)
- [x] Linhas sem `Liderança` não são mais ignoradas (permite cadastro sem nome).
- [x] Correção de IDs duplicados usando a linha da planilha (`index + 2`).
- [x] Campos vazios chegam como `""` (não null).

---

## ✅ Fase 2 — Estrutura Android (Concluída)

*(Estrutura base, dependências e Room/Retrofit — verórico acima para detalhes)*

---

## ✅ Fase 3 — Correções de runtime (Concluída)

*(Problemas de navegação e exibição — resolvidos)*

---

## ✅ Fase 4 — UI Compose (Concluída)

*(Telas implementadas e integradas com viewmodels)*

---

## ✅ Fase 5 — Validação no emulador (Concluída)

Durante a fase de testes e validação com o usuário, diversas melhorias cruciais foram implementadas no aplicativo para torná-lo mais inteligente, bonito e livre de bugs:

### Melhorias Implementadas:
1. **Contagem e Nomes Vazios:**
   - Contatos sem nome cadastrado agora aparecem como `(Sem nome)` e com avatar `?`.
   - Implementado contador total no formato `"X de Y contatos"`, buscando o número real de linhas do banco de dados (sempre exibe o total independentemente da busca).
2. **Atualização em Tempo Real:**
   - Transformação do campo de `ultimaSync` em *StateFlow*. Agora a TopBar é atualizada instantaneamente ao concluir a sincronização (não precisa mais fechar e abrir a tela).
3. **UI Edge-to-Edge Resolvida:**
   - Botão "Salvar na Agenda" na tela de detalhes estava sendo sobreposto pelos botões virtuais do sistema. Adicionado `navigationBarsPadding()` para respeitar a área do celular.
4. **Exportação para Agenda:**
   - Adicionado prefixo `"Observação: "` antes de enviar as notas para a agenda nativa do Android.
5. **Identidade Visual:**
   - Substituição do ícone padrão do Android pela logo `JP.png` do projeto em todos os diretórios `mipmap`.
   - Configuração de texto `"JP Contatos"` gerado em PNG no centro da Splash Screen inicial do Android 12+, ocultando a logo conforme requisitado.
6. **Pesquisa Inteligente (Smart Search):**
   - Criação da coluna `buscaNormalizada` na `ContatoEntity`.
   - Ao salvar os dados, o texto é formatado removendo acentos e convertendo para minúsculas.
   - O campo de busca pesquisa ignorando maiúsculas e acentuações (`João` = `joao`).
   - Aumento da versão do banco Room (v2) e aplicação de migração destrutiva automática.
7. **Sincronia de Estado na Navegação:**
   - Mudança de `termoBusca` para um estado lido diretamente do `ViewModel`.
   - Agora, ao clicar em um contato e retornar, o texto da pesquisa permanece preservado e coerente com a lista mostrada.

---

---

## ⏳ Fase 6 — Extras (Futuro)

- [ ] WorkManager — sync periódica em background (1x por hora)
- [ ] Migração para FTS4 se base crescer > 2.000 contatos
- [ ] Filtros por Região / Cidade (chips na tela de busca)

---

## Versões definitivas das dependências (após ajustes de compatibilidade)

| Biblioteca | Versão | Nota |
|------------|--------|------|
| AGP | 9.3.1 | Existente no projeto |
| Kotlin (embutido) | 2.1.0 | Via `libs.versions.toml` |
| KSP | 2.1.0-1.0.29 | Deve coincidir com versão Kotlin |
| Compose BOM | 2024.12.01 | Gerencia todas as libs Compose |
| **Room** | **2.8.4** | Fix bug KSP `jvm signature V` |
| Retrofit | 2.11.0 | — |
| Moshi | 1.15.1 | — |
| **Hilt** | **2.60.1** | Mínimo para AGP 9.x |
| Navigation Compose | 2.8.5 | — |
| WorkManager | 2.9.1 | — |
