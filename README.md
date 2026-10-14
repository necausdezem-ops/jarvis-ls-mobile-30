# J.A.R.V.I.S. Mobile — v3.1

Projeto Android com interface holográfica, núcleo ARC estilizado e notificações flutuantes animadas.

## Baixar o APK pelo celular

1. Abra a aba **Actions** deste repositório.
2. Selecione a execução **Build JARVIS Android APK**.
3. Espere a execução terminar com o status verde **Success**.
4. Em **Artifacts**, baixe **JARVIS-debug-apk** e extraia `app-debug.apk`.
5. Abra o APK no Android e autorize a instalação, se o sistema pedir.

O APK gerado é de depuração, destinado a testes. Cada push na branch `main` inicia uma nova compilação; também é possível iniciar manualmente pela opção **Run workflow**.

O código-fonte Android está compactado em partes base64 sob `source/` para manter o envio pelo GitHub íntegro. O workflow recompõe o arquivo antes de executar o Gradle.
