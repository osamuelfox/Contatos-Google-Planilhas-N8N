# JP Contatos — Technical Documentation

Este documento fornece uma visão aprofundada da arquitetura, fluxo de dados e decisões técnicas implementadas no projeto **JP Contatos**. Destinado a desenvolvedores e engenheiros de software que farão a manutenção ou escalabilidade da aplicação.

---

## 🏗️ Arquitetura e Padrões

O aplicativo segue os princípios da **Clean Architecture** alinhados ao padrão **MVVM (Model-View-ViewModel)** recomendado pelo Google para aplicações Android modernas. 

A abordagem central é **Offline-First**:
1. O banco de dados local (Room) é a **Single Source of Truth** (Única Fonte de Verdade).
2. A UI apenas observa fluxos reativos (`StateFlow`) emitidos pelo banco de dados.
3. A camada de rede (Retrofit) atua apenas como um sincronizador background que atualiza o banco local, provocando reatividade em cascata na UI.

---

## 🛠️ Stack Tecnológico e Versões

O projeto utiliza um conjunto de ferramentas "bleeding-edge", exigindo resoluções específicas de compatibilidade com o **Android Gradle Plugin (AGP) 9.3.1**.

| Camada | Tecnologia | Versão | Notas de Implementação |
|--------|------------|--------|------------------------|
| **Core** | Kotlin | `2.1.0` | Configurado via `libs.versions.toml`. O plugin `kotlin-android` foi removido do `build.gradle` pois o AGP 9.x já o embute nativamente. |
| **KSP** | Symbol Processing | `2.1.0-1.0.29` | Requer flag `android.disallowKotlinSourceSets=false` no `gradle.properties` devido a restrições do AGP 9.x. |
| **UI** | Jetpack Compose | BOM `2024.12.01` | Utiliza Material Design 3, `enableEdgeToEdge()` e `navigationBarsPadding`. |
| **DI** | Dagger Hilt | `2.60.1` | Atualização mandatória; versões anteriores (< 2.60) sofrem crash no AGP 9.x pela remoção da `BaseExtension`. |
| **Local** | Room | `2.8.4` | Resolve bug de KSP `jvm signature V`. Schema version 2, configurado com `fallbackToDestructiveMigration()`. |
| **Rede** | Retrofit / Moshi | `2.11.0` / `1.15.1` | Geração de adapters Moshi via KSP (`@JsonClass(generateAdapter = true)`). |
| **Navegação**| Nav Compose | `2.8.5` | Rotas string-based com `Uri.encode()` mandatório para IDs complexos. |

---

## 🔄 Fluxo de Dados e Integração (Middleware)

O backend do aplicativo não é um servidor tradicional, mas sim uma pipeline serverless via **n8n Cloud** conectada ao Google Sheets.

### Endpoint (n8n Webhook)
- **Método**: `GET`
- **URL**: `https://[SERVER]/webhook/contatos` (injetado via `BuildConfig.N8N_BASE_URL`)
- **Autenticação**: Header obrigatório `x-api-key` (injetado via `BuildConfig.N8N_API_KEY`)

### Transformação no n8n
1. Lê as linhas do Google Sheets.
2. Executa um Node JavaScript customizado para normalizar chaves vazias para `""`.
3. Associa IDs únicos aos registros baseados no índice da linha na planilha (`index + 2`) para evitar colisões entre pessoas homônimas de mesma cidade.
4. Empacota a resposta no formato:
   ```json
   {
     "atualizadoEm": "12/08 14:30",
     "contatos": [ { "id": "2", "nome": "...", ... } ]
   }
   ```

### Sincronização Local (Repository)
A sincronização possui um **throttle de 15 minutos** (gerenciado via `SharedPreferences`). 
Ao ser chamada, a camada de repositório (`ContatoRepository`):
1. Baixa a lista completa via Retrofit.
2. Mapeia de `ContatoDto` para `ContatoEntity`, gerando colunas computadas (ex: `buscaNormalizada`).
3. Executa `dao.limpar()` seguido de `dao.inserirTodos()`.
4. Devido ao `Flow` do Room, a UI atualiza automaticamente sem callbacks adicionais.

---

## 🧠 Soluções Técnicas de Destaque

### 1. Smart Search (Pesquisa Inteligente via SQLite)
A pesquisa por texto em SQLite (`LIKE`) falha nativamente em ignorar acentos e caracteres especiais. Para resolver isso sem depender da extensão FTS4 (Full-Text Search), aplicamos **Normalização no momento da inserção**:
- O `ContatoRepository` contém uma *extension function* `removerAcentos()` que utiliza `java.text.Normalizer` para limpar diacríticos.
- A Entidade `ContatoEntity` ganha uma coluna `buscaNormalizada`.
- A Query Room compara o termo do usuário (também normalizado em runtime pelo ViewModel) diretamente com a coluna `buscaNormalizada`.

### 2. Sincronia de Estado na Navegação
O `termoBusca` não fica armazenado na View (`BuscaScreen`), mas sim como um `MutableStateFlow` no `BuscaViewModel`. 
- **Benefício**: Ao utilizar o Jetpack Navigation para a tela de Detalhes e retornar, o ciclo de vida do ViewModel persiste, mantendo o campo de texto e o fluxo da `LazyColumn` exatamente na mesma posição e com o mesmo filtro anterior.

### 3. Segurança no Navigation Compose (URL Encoding)
Na versão utilizada do Navigation, rotas são definidas via String (ex: `detalhe/{id}`). Quando IDs contêm caracteres especiais como `/`, `?` ou espaços, o Compose fatalmente lança uma `IllegalArgumentException`. 
- **Solução**: Todos os argumentos de navegação passam por `Uri.encode(id)` no lado da chamada e são recuperados de forma decodificada e segura pelo NavBackStackEntry.

---

## 🚀 Próximos Passos (Tech Debt / Futuro)
- **WorkManager**: Implementar rotina em background (ex: 1x por dia ou hora) para manter o cache aquecido antes mesmo de o usuário abrir o app.
- **Room FTS4**: Se o Google Sheets ultrapassar 2.000 a 5.000 linhas de contatos, a coluna `buscaNormalizada` deve ser migrada para uma tabela virtual FTS4 para manter a performance da digitação em `< 16ms` (evitar pular frames de UI na digitação).
