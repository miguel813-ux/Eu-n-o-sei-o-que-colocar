# Universal Piano — ETAPA 1: Planejamento e Arquitetura

Status: planejamento concluído. Nenhum código de funcionalidade foi escrito ainda (isso começa na ETAPA 2). Este documento define decisões, limitações e o que será verificado antes de avançar.

---

## 1. Visão geral da arquitetura

Arquitetura em **camadas**, multi-módulo Gradle, com dependências sempre apontando para baixo (UI nunca conhece áudio nativo; áudio nunca conhece UI).

```
                    ┌──────────────┐
                    │     app      │  Application, MainActivity, navegação, DI
                    └──────┬───────┘
        ┌──────────┬───────┼─────────┬──────────────┐
        ▼          ▼       ▼         ▼              ▼
     ┌─────┐  ┌────────┐ ┌────────┐ ┌─────────────┐ ┌──────────┐
     │ ui  │  │keyboard│ │recorder│ │compatibility│ │ settings │
     └──┬──┘  └───┬────┘ └───┬────┘ └──────┬──────┘ └────┬─────┘
        │         │          │             │             │
        └─────────┴────┬─────┴─────────────┘             │
                       ▼                                 ▼
                ┌────────────┐   ┌──────┐          ┌──────────┐
                │ instruments│   │ midi │          │ database │
                └─────┬──────┘   └──┬───┘          └──────────┘
                      ▼             │
                  ┌───────┐         │
                  │ audio │◄────────┘ (midi só gera eventos; audio só consome)
                  └───────┘  (Kotlin + C++/Oboe)
```

Regra de ouro: **`audio` e `midi` não dependem de Android UI**; `keyboard` emite `NoteEvent` (evento puro), nunca chama o motor de áudio diretamente — quem conecta é o `app` via um `NoteBus`.

### Módulos

| Módulo | Responsabilidade | Depende de |
|---|---|---|
| `app` | Application, Activity única, NavHost, injeção de dependências (Hilt ou DI manual), manifesto | todos |
| `ui` | Design system Compose (tema, cores, componentes, ícones), telas (Home, Instrumentos, Biblioteca, Configurações, Layouts, Compatibilidade) | `keyboard`, `settings` |
| `keyboard` | Composable do teclado, layout de teclas, multitouch, mapeamento nota↔tecla, emissão de `NoteEvent` | (núcleo puro) |
| `audio` | Motor de áudio: Oboe/AAudio em C++, mixer, pool de vozes, envelopes, fallback AudioTrack | — |
| `instruments` | Definição dos 7 instrumentos (amostras + síntese), carregamento/descarregamento de samples | `audio` |
| `midi` | Parser/gravador de arquivos .mid (SMF), conversão sequência↔eventos | — |
| `recorder` | Gravação de `NoteEvent` com timestamps, reprodução própria, loop, velocidade, modo treino | `midi` |
| `compatibility` | Detecção e relatório de métodos de integração viáveis; overlay flutuante; IME; acessibilidade (somente autorizada) | — |
| `settings` | Preferências (DataStore) e perfis de configuração/layouts | `database` |
| `database` | Room: gravações, histórico, biblioteca, layouts salvos, mapeamentos | — |
| `tests` | Testes instrumentados e de desempenho (latência, multitouch, memória) | todos |

(Obs.: `tests/` na raiz guarda scripts e relatórios; testes unitários ficam em `src/test` de cada módulo.)

---

## 2. Tecnologias escolhidas — vantagens e limitações

### Kotlin + Android Studio
- **Vantagens:** linguagem oficial, coroutines/Flow para eventos, interoperabilidade com C++ via JNI.
- **Limitações:** GC pode causar pausas — por isso **nada de alocação na thread de áudio** (o loop de áudio fica em C++).

### Jetpack Compose (UI)
- **Vantagens:** UI declarativa, animações suaves nativas, adaptação a telas variadas, temas dinâmicos fáceis.
- **Limitações:** desenhar o teclado como muitos composables individuais seria lento. **Decisão:** o teclado é um único `Canvas` com `pointerInput` customizado (hit-test próprio), não N botões.

### Oboe (C++) sobre AAudio/OpenSL ES — **escolhido para o motor principal**
- **Vantagens:** biblioteca oficial do Google; usa AAudio (Android 8.1+) com fallback automático para OpenSL ES; modo `LowLatency` + `PerformanceMode::LowLatency` + `SharingMode::Exclusive` quando disponível; callback em thread de tempo real.
- **Limitações:** exige NDK/CMake e C++; latência real depende do hardware (típico 10–40 ms; varia por aparelho); modo exclusivo pode ser negado.
- **Dependência:** `com.google.oboe:oboe` via Prefab (AAR oficial no Maven Google). Versão exata será fixada e verificada no build da ETAPA 4 (não assumo número de versão aqui).

### AudioTrack — **fallback**
- **Vantagens:** só Kotlin/Java, sem NDK; `PERFORMANCE_MODE_LOW_LATENCY` já existe.
- **Limitações:** latência normalmente maior e menos previsível; usado apenas se Oboe falhar ao abrir o stream.

### Síntese + amostras (instrumentos)
- **Decisão:** abordagem **híbrida**.
  - *Piano acústico, piano elétrico, harpa, caixa de música:* amostras (poucas notas-âncora por instrumento, com pitch-shift/interpolação para as demais), para timbre realista.
  - *Órgão, sintetizador, piano digital:* síntese aditiva/subtrativa em C++ (osciladores + filtro + envelope ADSR).
- **Limitação importante:** as amostras precisam ter **licença livre compatível** (ex.: CC0 / domínio público). Não vou embutir amostras de licença duvidosa. Na ETAPA 4 vou listar a origem e licença de cada arquivo; se eu não tiver acesso a amostras livres no ambiente, uso síntese mais elaborada (aditiva com inarmônicos + ruído de ataque) e aviso explicitamente.

### Room (armazenamento local)
- **Vantagens:** SQLite tipado, migrações, Flow reativo.
- **Limitações:** não usar para dados de alta frequência. Guarda: gravações, histórico, biblioteca, layouts, mapeamentos. Configurações simples ficam em **DataStore**.

### MIDI (arquivos .mid)
- **Decisão:** implementar parser/escritor SMF (formato 0/1) próprio e pequeno em Kotlin puro — sem dependência frágil, totalmente testável por unidade.
- **Limitação:** foco em eventos Note On/Off, tempo e programa; meta-eventos avançados serão ignorados com segurança.
- `android.media.midi` (MIDI USB/BLE em tempo real) fica como extra opcional, não requisito.

### AccessibilityService — **uso restrito**
- **Vantagens:** é a única API oficial que permite gestos sintéticos em outros apps (`dispatchGesture`).
- **Limitações:** exige que o usuário ative manualmente; a Play Store tem política rígida (só para ajudar usuários com deficiência); gestos são toques em coordenadas, não "teclas". Ver seção 4 — **não será usado por padrão**.

### Overlay (`SYSTEM_ALERT_WINDOW` / `TYPE_APPLICATION_OVERLAY`)
- **Vantagens:** janela flutuante oficial sobre outros apps, com permissão concedida pelo usuário.
- **Limitações:** o overlay **não injeta entrada em outros apps**. Ele só mostra o piano por cima e toca som próprio. Alguns jogos bloqueiam ou ocultam overlays.

---

## 3. Áudio: como as notas serão processadas

Pipeline planejado:

1. **UI thread (Compose):** detecta toque → cria `NoteEvent(noteMidi, velocity, timestamp, on/off)`.
2. **Fila lock-free (SPSC ring buffer):** evento entra sem alocação nem mutex. Kotlin escreve via JNI; o callback de áudio lê.
3. **Callback Oboe (thread de tempo real):**
   - drena a fila de eventos;
   - aloca uma **voz** do pool fixo (ex.: 32–64 vozes pré-alocadas);
   - cada voz gera amostras (sample playback ou oscilador) com envelope ADSR;
   - **mixer** soma vozes, aplica ganho individual e mestre, com soft-clip/limiter para evitar estalos;
   - preenche o buffer de saída em `float`.
4. **Roubo de voz:** se o pool esgota, rouba a voz mais antiga/mais baixa em volume (com fade de ~5 ms para não estalar).
5. **Anti-estalo:** todo note-off tem release curto; todo note-on tem attack de poucos ms; mudanças de volume são suavizadas (rampa).
6. **Memória:** samples decodificados para PCM 16-bit mono, carregados só para o instrumento ativo e liberados na troca; buffers do Oboe fechados em `onStop/onDestroy`.
7. **Latência:** buffer = 2× burst size do dispositivo; medida e reportada na ETAPA 4/8 (medição real exige aparelho — ver seção 6).

### Multitouch
- Um único `pointerInput` no Canvas do teclado, usando `awaitPointerEventScope` e rastreando cada `PointerId`.
- Mapa `pointerId → tecla atual`. Em `Down`: hit-test (pretas têm prioridade sobre brancas na área sobreposta) → note-on. Em `Move`: se o dedo entra em outra tecla, faz glissando (note-off antiga + note-on nova, configurável). Em `Up/Cancel`: note-off.
- Velocidade (sensibilidade): derivada da posição vertical do toque na tecla e/ou configuração de sensibilidade (Android não fornece pressão confiável na maioria das telas capacitivas).
- Sem recomposição por toque: o estado de teclas pressionadas é um bitset lido apenas na fase de desenho.

---

## 4. Integração com jogos: limitações do Android (investigação)

Premissa do projeto: **só métodos tecnicamente viáveis e autorizados**. Resumo do que é e do que não é possível:

| Método | O que faz | Funciona para "tocar no jogo"? | Observações |
|---|---|---|---|
| Piano independente (som próprio) | Toca no alto-falante do celular | Parcial: o jogo pode captar pelo microfone se ele próprio tiver essa função | Não envia nada ao jogo |
| Overlay flutuante | Mostra o teclado sobre o jogo | **Não** envia entradas ao jogo | Útil como "segunda tela"/guia visual e para tocar som próprio; sujeito a bloqueio por apps |
| IME (teclado virtual) | Envia texto/teclas ao campo de texto focado | **Somente** se o jogo aceitar entrada de teclado/texto via IME | A maioria dos jogos com instrumento (touch) não usa IME |
| Acessibilidade `dispatchGesture` | Simula toques em coordenadas da tela | Tecnicamente possível, **mas** exige habilitação manual, política da Play Store restritiva e pode violar os termos de uso do jogo | Será **opcional, desativado por padrão**, com aviso claro e sem ler conteúdo de outros apps |
| Exportação de sequências (MIDI/lista de notas) | Gera arquivo para o usuário usar onde for aceito | Sim, onde o jogo/app importar | Totalmente seguro |
| MIDI em tempo real (USB/BLE) | Envia a dispositivos MIDI externos | Só para apps/jogos que aceitem MIDI | Opcional |
| Injeção de código, memória, hooks | — | **Fora de escopo** | Não será implementado |

**Modo de compatibilidade (design):** a tela "Compatibilidade com jogos" terá um *relatório por método* com estado (`Disponível` / `Requer permissão` / `Indisponível neste aparelho`) e uma explicação do que cada método realmente faz. Não vou marcar nenhum jogo específico como "compatível" sem teste real — o catálogo de jogos começa vazio e só recebe entradas com método e resultado verificados.

**Aviso honesto:** muitos jogos mobile com instrumentos aceitam apenas toques na própria tela. Nesses casos, o caminho realista é o piano independente + overlay como referência visual + exportação de sequência. Simular toques (acessibilidade) pode ser bloqueado pelo jogo ou violar seus termos; deixo a decisão e o risco explícitos para o usuário.

---

## 5. Privacidade e permissões

- Permissões mínimas por padrão: **nenhuma** para o piano independente.
- `SYSTEM_ALERT_WINDOW`: só ao ativar o overlay (fluxo oficial de concessão).
- Acessibilidade: só se o usuário ativar manualmente; serviço configurado para **não ler conteúdo de janelas** (`canRetrieveWindowContent=false`), sem log de eventos.
- Sem INTERNET no manifesto por padrão. Sem analytics. Gravações e layouts apenas locais (Room/arquivos privados do app).
- Sem captura de tela, sem leitura de entradas em outros apps.

---

## 6. Ambiente e verificação (o que posso e não posso testar)

Honestidade sobre o ambiente:
- Posso **compilar e rodar testes unitários** (parser MIDI, lógica de teclas, mixer em JVM/C++ host) se o SDK/NDK/Gradle puderem ser instalados aqui — isso será verificado no início da ETAPA 2, antes de escrever a UI. O acesso de rede do ambiente é limitado a repositórios de pacotes; se o download do Android SDK for bloqueado, aviso e entrego o projeto pronto para compilar no seu PC/Android Studio, em vez de afirmar que o APK foi gerado.
- **Latência real e multitouch físico não podem ser medidos aqui** (não há aparelho). Esses testes ficam como roteiro para você rodar no seu Poco X7 Pro, e só reportarei números que realmente existirem.

---

## 7. Plano das etapas (com critério de aceite)

| Etapa | Entrega | Critério para avançar |
|---|---|---|
| 2 — Interface | Tema, navegação, tela inicial com 6 cartões, configurações base | Projeto compila; navegação entre telas verificada |
| 3 — Teclado | Canvas com teclas brancas/pretas, oitavas, multitouch | Testes unitários de hit-test e do mapa de ponteiros passam |
| 4 — Áudio | Oboe + mixer + 7 instrumentos | Testes de mixer/voz (sem clipping, sem vazamento de vozes); build nativo OK |
| 5 — Personalização | Temas, perfis, layouts salvos (DataStore/Room) | Persistência verificada em teste |
| 6 — Integração | Overlay, relatório de compatibilidade, exportação | Cada método com estado real detectado; doc de compatibilidade |
| 7 — Avançados | Gravador, reprodução, MIDI import/export, biblioteca | Round-trip MIDI (exportar→importar) idêntico em teste |
| 8 — Testes | Roteiros e relatórios | Falhas listadas e corrigidas |
| 9 — Compilação | APK + instruções | APK só entregue se de fato compilado aqui |

---

## 8. Estrutura de pastas criada

```
UniversalPiano/
├── app/            (src/main/java/com/universalpiano/app, res, assets)
├── audio/          (kotlin + cpp para Oboe)
├── keyboard/
├── instruments/    (assets/samples)
├── midi/
├── recorder/
├── compatibility/
├── settings/
├── database/
├── ui/
├── assets/
├── tests/
└── docs/           (este documento, futura doc de compatibilidade)
```

---

## 9. Decisões em aberto (preciso de confirmação antes da ETAPA 2)

1. **minSdk:** proponho **26** (Android 8.0; AAudio de baixa latência a partir de 8.1 com fallback). Seu alvo (Poco X7 Pro) roda Android muito mais novo — posso subir o minSdk se preferir simplificar.
2. **DI:** Hilt (padrão, mais código gerado) ou DI manual (mais leve). Sugestão: **DI manual** neste porte.
3. **Overlay e acessibilidade:** manter a acessibilidade como opcional desativada por padrão (recomendado) ou omitir por completo?
