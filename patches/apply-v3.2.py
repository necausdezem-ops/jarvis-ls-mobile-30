from pathlib import Path
import shutil

root = Path("JARVIS")
overlay = Path("overlays")

def replace_once(text, old, new, label):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Patch error ({label}): expected one match, found {count}")
    return text.replace(old, new, 1)

for source, target in [
    (overlay / "java/com/cris/jarvis/actions/DeviceCommandExecutor.kt", root / "app/src/main/java/com/cris/jarvis/actions/DeviceCommandExecutor.kt"),
    (overlay / "java/com/cris/jarvis/actions/JarvisAccessibilityService.kt", root / "app/src/main/java/com/cris/jarvis/actions/JarvisAccessibilityService.kt"),
    (overlay / "java/com/cris/jarvis/ui/LauncherScreen.kt", root / "app/src/main/java/com/cris/jarvis/ui/LauncherScreen.kt"),
    (overlay / "res/xml/jarvis_accessibility_service.xml", root / "app/src/main/res/xml/jarvis_accessibility_service.xml"),
    (overlay / "res/values/strings.xml", root / "app/src/main/res/values/strings.xml"),
]:
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, target)

p = root / "app/src/main/java/com/cris/jarvis/MainActivity.kt"
s = p.read_text()
s = replace_once(s, "import com.cris.jarvis.ui.CoreScreen\n", "import androidx.compose.animation.AnimatedContent\nimport androidx.compose.animation.fadeIn\nimport androidx.compose.animation.fadeOut\nimport androidx.compose.animation.scaleIn\nimport androidx.compose.animation.scaleOut\nimport androidx.compose.animation.togetherWith\nimport androidx.compose.animation.core.tween\nimport com.cris.jarvis.ui.CoreScreen\nimport com.cris.jarvis.ui.LauncherScreen\n", "MainActivity import")
s = replace_once(s, 'val names = listOf("NÚCLEO", "HOLO", "IA", "AJUSTES")\n    val icons = listOf("◉", "▣", "✦", "⚙")\n    val subtitles = listOf("CORE INTERFACE", "HOLOGRAM FEED", "AI NETWORK", "SYSTEM CONFIG")', 'val names = listOf("INÍCIO", "NÚCLEO", "HOLO", "IA", "AJUSTES")\n    val icons = listOf("⌂", "◉", "▣", "✦", "⚙")\n    val subtitles = listOf("PERSONAL HOME", "CORE INTERFACE", "HOLOGRAM FEED", "AI NETWORK", "SYSTEM CONFIG")', "navigation labels")
s = replace_once(s, '''                when (tab) {
                    0 -> CoreScreen(prefs)
                    1 -> HoloScreen()
                    2 -> AiScreen(prefs)
                    else -> SettingsScreen(prefs)
                }''', '''                AnimatedContent(
                    targetState = tab,
                    label = "jarvis-hologram-tab",
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(420)) + scaleIn(initialScale = 0.90f, animationSpec = tween(420)))
                            .togetherWith(fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 1.06f, animationSpec = tween(180)))
                    }
                ) { targetTab ->
                    when (targetTab) {
                        0 -> LauncherScreen()
                        1 -> CoreScreen(prefs)
                        2 -> HoloScreen()
                        3 -> AiScreen(prefs)
                        else -> SettingsScreen(prefs)
                    }
                }''', "tab destinations")
p.write_text(s)

p = root / "app/src/main/java/com/cris/jarvis/ui/Screens.kt"
s = p.read_text()
s = replace_once(s, "import android.content.Intent\n", "import android.content.Intent\nimport android.speech.RecognizerIntent\n", "speech import")
s = replace_once(s, "import androidx.compose.foundation.layout.ColumnScope\n", "import androidx.compose.foundation.layout.ColumnScope\nimport androidx.compose.foundation.layout.PaddingValues\n", "PaddingValues import")
s = replace_once(s, "import androidx.compose.runtime.Composable\n", "import androidx.activity.compose.rememberLauncherForActivityResult\nimport androidx.activity.result.contract.ActivityResultContracts\nimport androidx.compose.runtime.Composable\n", "speech launcher imports")
s = replace_once(s, "import com.cris.jarvis.ai.AiHub\n", "import com.cris.jarvis.actions.DeviceCommandExecutor\nimport com.cris.jarvis.ai.AiHub\n", "local action import")
s = replace_once(s, '''fun CoreScreen(prefs: Prefs) {
    val speaking by Speaker.speaking.collectAsState()''', '''fun CoreScreen(prefs: Prefs) {
    val ctx = LocalContext.current
    val speaking by Speaker.speaking.collectAsState()''', "chat context")
s = replace_once(s, '''    var input by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()''', '''    var input by remember { mutableStateOf("") }
    var sendAfterVoice by remember { mutableStateOf(false) }
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
        if (spoken.isNotBlank()) {
            input = spoken
            sendAfterVoice = true
        }
    }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()''', "speech state")
s = replace_once(s, '''                try {
                    val history = msgs.filter { it.role == "user" || it.role == "assistant" }.map { it.role to it.text }
                    val r = AiHub.ask(prefs, history)
                    msgs.add(ChatMsg("assistant", r.text, r.provider))
                    if (prefs.speakResponses) Speaker.speak(r.text)
                } catch (e: Exception) {''', '''                try {
                    val localAction = DeviceCommandExecutor.execute(ctx, q)
                    if (localAction != null) {
                        msgs.add(ChatMsg("assistant", localAction, "CONTROLE LOCAL"))
                        if (prefs.speakResponses) Speaker.speak(localAction)
                        return@launch
                    }
                    val history = msgs.filter { it.role == "user" || it.role == "assistant" }.map { it.role to it.text }
                    val r = AiHub.ask(prefs, history)
                    msgs.add(ChatMsg("assistant", r.text, r.provider))
                    if (prefs.speakResponses) Speaker.speak(r.text)
                } catch (e: Exception) {''', "local action hook")
s = replace_once(s, '''    Column(
        Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 9.dp),''', '''    LaunchedEffect(sendAfterVoice, input, thinking) {
        if (sendAfterVoice && input.isNotBlank() && !thinking) {
            sendAfterVoice = false
            send()
        }
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 9.dp),''', "auto send voice")
s = replace_once(s, '''            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { send() },''', '''            Spacer(Modifier.width(5.dp))
            OutlinedButton(
                onClick = {
                    val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                        .putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga seu comando para o JARVIS")
                    try { voiceLauncher.launch(speechIntent) }
                    catch (_: Exception) { msgs.add(ChatMsg("error", "Não encontrei um serviço de reconhecimento de voz neste aparelho. Você ainda pode digitar o comando.", null)) }
                },
                border = BorderStroke(1.dp, Neon.Cyan.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(9.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
            ) { Text("🎙", fontSize = 15.sp) }
            Spacer(Modifier.width(5.dp))
            Button(
                onClick = { send() },''', "voice button")
s = replace_once(s, 'Text("Estou pronto. Digite uma mensagem para iniciar a conversa.", color = Neon.Dim, fontSize = 12.sp, lineHeight = 17.sp)', 'Text("Estou pronto. Digite ou toque no microfone e diga um comando, como ‘abrir WhatsApp’ ou ‘instalar Spotify’.", color = Neon.Dim, fontSize = 12.sp, lineHeight = 17.sp)', "chat hint")
s = replace_once(s, '''    val battOn = tick >= 0 &&
        (ctx.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(ctx.packageName)
''', '''    val battOn = tick >= 0 &&
        (ctx.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(ctx.packageName)
    val accessibilityOn = tick >= 0 &&
        (Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.split(':')?.any { it.contains(ctx.packageName, ignoreCase = true) } == true)
''', "accessibility state")
s = replace_once(s, '''        Title("PERMISSÕES")
        Panel {
            PermRow("Acesso às notificações", listenerOn, "ATIVAR") {''', '''        Title("PERMISSÕES")
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("JARVIS COMO TELA INICIAL", color = Neon.Text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Faz a interface JARVIS aparecer ao apertar o botão Início.", color = Neon.Dim, fontSize = 10.sp, lineHeight = 14.sp)
                }
                OutlinedButton(onClick = {
                    try { ctx.startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
                    catch (_: Exception) { ctx.startActivity(Intent(Settings.ACTION_SETTINGS)) }
                }, border = BorderStroke(1.dp, Neon.Cyan.copy(alpha = 0.8f))) {
                    Text("ESCOLHER", fontSize = 9.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            PermRow("Controle do sistema (acessibilidade)", accessibilityOn, "ATIVAR") {
                ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            Text("Opcional. Autoriza apenas comandos globais de voltar, tela inicial, apps recentes e painéis do Android. O JARVIS não lê o conteúdo da tela nem toca em botões de outros apps.", color = Neon.Dim, fontSize = 10.sp, lineHeight = 14.sp, modifier = Modifier.padding(top = 3.dp))
            Spacer(Modifier.height(10.dp))
            PermRow("Acesso às notificações", listenerOn, "ATIVAR") {''', "settings controls")
p.write_text(s)

p = root / "app/src/main/AndroidManifest.xml"
s = p.read_text()
s = replace_once(s, '<manifest xmlns:android="http://schemas.android.com/apk/res/android"', '<manifest xmlns:android="http://schemas.android.com/apk/res/android"\n    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />', "launcher package visibility")
s = replace_once(s, '''            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>''', '''            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.HOME" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>''', "home launcher category")
s = replace_once(s, '''        <service
            android:name=".notify.JarvisNotificationListener"''', '''        <service
            android:name=".actions.JarvisAccessibilityService"
            android:exported="true"
            android:label="JARVIS — Controle do sistema"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/jarvis_accessibility_service" />
        </service>

        <service
            android:name=".notify.JarvisNotificationListener"''', "accessibility service")
p.write_text(s)

p = root / "app/build.gradle.kts"
s = p.read_text()
s = replace_once(s, "versionCode = 2", "versionCode = 3", "version code")
s = replace_once(s, 'versionName = "3.1.0"', 'versionName = "3.2.0"', "version name")
p.write_text(s)

print("Applied JARVIS v3.2")
