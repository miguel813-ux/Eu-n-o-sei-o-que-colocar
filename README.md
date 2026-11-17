# Universal Piano

App Android de piano (Kotlin + Jetpack Compose). Arquitetura e decisões em `docs/ARQUITETURA.md`.

## Estado atual: ETAPA 2 (interface)

Implementado:
- Projeto Gradle multi-módulo: `app`, `ui`, `settings` (os outros módulos existem só como pasta, para as próximas etapas)
- Tema Material 3 (claro/escuro/sistema + 3 cores de destaque)
- Tela inicial com 6 cartões e navegação (NavHost)
- Tela de Configurações persistida em DataStore
- Telas provisórias que dizem em qual etapa cada seção será feita

Decisões adotadas da seção 9 do documento: minSdk 26, DI manual, acessibilidade opcional e desativada.

## Como compilar

1. Abra a pasta `UniversalPiano` no Android Studio (versão recente, com JDK 17+).
2. Aguarde o Gradle sincronizar (o Android Studio gera o wrapper e baixa o SDK 35 se faltar).
3. Rode no aparelho (Poco X7 Pro) ou em um emulador.

Teste unitário do módulo `settings`: `./gradlew :settings:testDebugUnitTest`

## Aviso de verificação

Esta etapa foi escrita em um ambiente sem Android SDK e sem acesso aos repositórios do Google/Maven,
então **não foi compilada nem executada**. As versões em `gradle/libs.versions.toml` são versões
reais e estáveis, mas não foram resolvidas por um build. Se o sync acusar algum erro, ele deve ser
de versão/dependência e é simples de ajustar.
