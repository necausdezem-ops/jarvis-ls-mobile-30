# J.A.R.V.I.S. Mobile — v3.2

Launcher Android nativo com interface HUD/holográfica, lista de aplicativos instalados, comandos locais e entrada por voz em português.

## Definir como tela inicial
1. Instale e abra o JARVIS.
2. Entre em **INÍCIO** e toque em **DEFINIR JARVIS COMO TELA INICIAL**.
3. Escolha JARVIS como app de início. Isso permite que o botão Início do Android retorne ao launcher JARVIS.

## Comandos de exemplo
- `abrir WhatsApp`, `abrir YouTube`, `abrir Chrome` ou outro app instalado.
- `pesquisar previsão do tempo` ou `abrir site wikipedia.org`.
- `abrir configurações de Wi-Fi`, `abrir Bluetooth`, `apps recentes`, `voltar`, `abrir notificações` e `abrir painel rápido`.
- `ligar para 11999990000` abre o discador; você confirma a chamada.
- `instalar Spotify` abre a pesquisa da Play Store; você confirma a instalação.

## Permissões importantes
Ative **Acesso às notificações** e **Exibir sobre outros apps** para os hologramas de notificações.
O **Controle do sistema (acessibilidade)** é opcional: ao ativá-lo manualmente em Configurações, o JARVIS ganha ações globais de voltar/início/recentes/notificações/painel rápido. O serviço não lê o conteúdo da tela nem clica em botões de outros apps.

Um app Android comum não pode instalar ou desinstalar aplicativos silenciosamente; o fluxo de instalação continua mostrando a tela oficial do Android/Play Store e exige confirmação do usuário.

## Baixar o APK
Abra **Actions** → **Build JARVIS Android APK** → a execução mais recente com status **Success** → artefato **JARVIS-debug-apk**. É um APK de depuração para testes.
