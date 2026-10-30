# JP Contatos (Google Planilhas)

Um aplicativo Android moderno desenvolvido para gerenciamento e busca rápida de contatos e lideranças. Os dados são mantidos e administrados em uma planilha do Google Sheets e sincronizados com o aplicativo através de um webhook no **n8n**.

## 🚀 Funcionalidades

- **Sincronização Nuvem-Local:** Os contatos cadastrados no Google Sheets são puxados para o banco local do celular, permitindo o uso do app totalmente **offline**.
- **Pesquisa Inteligente (Smart Search):** Busca de contatos por nome, cidade ou região que ignora letras maiúsculas/minúsculas e acentuação gráfica (ex: buscar por "joao" encontra "João").
- **Integração com Agenda Nativa:** Permite salvar um contato diretamente na agenda do celular com apenas um clique, exportando também as notas, cidade e região automaticamente.
- **Controle de Estado Fluido:** Mantém a sua pesquisa salva e visível enquanto você navega entre a lista e o perfil dos contatos.
- **Identidade Visual e Splash Screen:** Ícone personalizado para a tela inicial e uma *Splash Screen* customizada (Android 12+) focada na leitura e carregamento imediato.

## 🛠️ Tecnologias Utilizadas (Tech Stack)

O projeto foi construído utilizando as ferramentas e padrões mais modernos do ecossistema Android nativo:

- **Linguagem:** Kotlin 2.1.0
- **UI Toolkit:** Jetpack Compose (Material Design 3)
- **Arquitetura:** MVVM (Model-View-ViewModel) + Clean Architecture (Offline-first)
- **Banco de Dados Local:** Room Database (SQLite)
- **Injeção de Dependência:** Dagger Hilt
- **Comunicação de Rede:** Retrofit + Moshi (para serialização JSON)
- **Programação Reativa e Assíncrona:** Kotlin Coroutines e StateFlow
- **Navegação:** Jetpack Navigation Compose
- **Backend/Middleware:** n8n Cloud (via Webhook) integrado ao Google Sheets

## ⚙️ Como configurar e rodar o projeto

1. **Clone ou baixe o repositório.**
2. **Abra o projeto no Android Studio** (Recomendado utilizar uma versão recente, compatível com AGP 9.x).
3. **Configure as chaves de acesso:**
   Na raiz do projeto, crie ou edite o arquivo `local.properties` e adicione as duas variáveis abaixo referentes ao seu servidor n8n:
   ```properties
   N8N_BASE_URL="https://sua-url-do-n8n.com/"
   N8N_API_KEY="seu_token_de_acesso_aqui"
   ```
4. **Sincronize o projeto** (`Sync Project with Gradle Files`).
5. **Compile e execute:** Pressione **Run** (Shift + F10) ou use o comando no terminal para gerar o APK:
   ```bash
   ./gradlew assembleDebug
   ```

## 📦 Gerando o APK para envio

Se você deseja enviar o app pronto para uso interno da equipe:
1. Pelo Android Studio, vá em **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Ou via terminal: `./gradlew assembleDebug`
3. O arquivo final estará disponível em:
   `app/build/outputs/apk/debug/app-debug.apk`

---
*Desenvolvido para acompanhamento eficiente de contatos e metas.*
