package com.cris.jarvis.actions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import java.text.Normalizer
import java.util.Locale

data class LaunchableApp(val packageName: String, val label: String, val icon: Drawable)

object DeviceCommandExecutor {
    fun installedApps(context: Context): List<LaunchableApp> {
        val pm = context.packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return try {
            pm.queryIntentActivities(launcher, PackageManager.MATCH_DEFAULT_ONLY)
                .mapNotNull { r ->
                    val info = r.activityInfo ?: return@mapNotNull null
                    if (info.packageName == context.packageName) return@mapNotNull null
                    LaunchableApp(info.packageName, r.loadLabel(pm)?.toString()?.trim().orEmpty().ifBlank { info.packageName }, r.loadIcon(pm))
                }
                .distinctBy { it.packageName }
                .sortedBy { norm(it.label) }
        } catch (_: Exception) { emptyList() }
    }

    fun launchApp(context: Context, app: LaunchableApp): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName) ?: return false
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (_: Exception) { false }
    }

    /** Returns a result for recognized local actions; null means normal AI chat. */
    fun execute(context: Context, command: String): String? {
        val raw = command.trim()
        if (raw.isBlank()) return null
        val q = norm(raw).replace(Regex("[!?.,;:]+$"), "").trim()
        val install = Regex("^(por favor\\s+)?(baixe|baixar|download|instale|instalar|instala)\\s+(o\\s+|a\\s+|um\\s+|uma\\s+)?(aplicativo\\s+|app\\s+)?")
        if (install.containsMatchIn(q)) {
            val name = q.replace(install, "").trim()
            return if (name.isNotBlank()) storeSearch(context, name) else "Qual aplicativo você quer procurar? Diga ‘instalar nome do app’."
        }

        if (q in setOf("play store", "abrir play store", "abre play store", "google play")) {
            return openIntent(context, Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=apps")), "Abrindo a Play Store.")
        }

        when (q) {
            "voltar", "voltar uma tela", "acao voltar" ->
                return globalOrHint(JarvisAccessibilityService.ACTION_BACK, "Voltando uma tela.")
            "apps recentes", "aplicativos recentes", "mostrar recentes" ->
                return globalOrHint(JarvisAccessibilityService.ACTION_RECENTS, "Abrindo os aplicativos recentes.")
            "abrir notificacoes", "mostrar notificacoes", "painel de notificacoes" ->
                return globalOrHint(JarvisAccessibilityService.ACTION_NOTIFICATIONS, "Abrindo suas notificações.")
            "painel rapido", "abrir painel rapido", "configuracoes rapidas" ->
                return globalOrHint(JarvisAccessibilityService.ACTION_QUICK_SETTINGS, "Abrindo as configurações rápidas.")
            "ir para inicio", "tela inicial", "voltar para inicio", "home", "inicio do celular", "ir para home" -> {
                if (JarvisAccessibilityService.globalAction(JarvisAccessibilityService.ACTION_HOME)) return "Voltando à tela inicial."
                return openIntent(context, Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), "Voltando à tela inicial do celular.")
            }
            "configuracoes", "configuracoes do sistema", "abrir configuracoes", "abre configuracoes", "configuracoes do celular", "ajustes do sistema" ->
                return openIntent(context, Intent(Settings.ACTION_SETTINGS), "Abrindo as configurações do Android.")
            "tirar foto", "fotografar" ->
                return openIntent(context, Intent(MediaStore.ACTION_IMAGE_CAPTURE), "Abrindo a câmera. Você decide quando tirar a foto.")
            "abrir contatos", "mostrar contatos", "meus contatos" ->
                return openIntent(context, Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI), "Abrindo seus contatos.")
        }

        val settings = when {
            (q.contains("wifi") || q.contains("wi fi")) && (q.contains("configur") || q.startsWith("abrir") || q.startsWith("abre")) -> Intent(Settings.ACTION_WIFI_SETTINGS)
            q.contains("bluetooth") && (q.contains("configur") || q.startsWith("abrir") || q.startsWith("abre")) -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            (q.contains("som") || q.contains("volume") || q.contains("audio")) && (q.contains("configur") || q.startsWith("abrir") || q.startsWith("abre")) -> Intent(Settings.ACTION_SOUND_SETTINGS)
            (q.contains("tela") || q.contains("brilho") || q.contains("display")) && (q.contains("configur") || q.startsWith("abrir") || q.startsWith("abre")) -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            q.contains("bateria") && (q.contains("configur") || q.startsWith("abrir") || q.startsWith("abre")) -> Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            q.contains("acessibilidade") && (q.contains("configur") || q.startsWith("abrir") || q.startsWith("abre")) -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            q.contains("tela inicial padrao") || q.contains("escolher launcher") || q.contains("launcher padrao") -> Intent(Settings.ACTION_HOME_SETTINGS)
            q.contains("gerenciar aplicativos") || q.contains("lista de aplicativos") -> Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            else -> null
        }
        if (settings != null) return openIntent(context, settings, "Abrindo a tela solicitada nas configurações.")

        if (q.startsWith("ligar para ") || q.startsWith("ligue para ") || q.startsWith("discar ")) {
            val target = q.substringAfter("para ", q.substringAfter("discar ", "")).trim()
            val number = target.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
            return if (number.count { it.isDigit() } >= 3) {
                openIntent(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}")), "Abri o discador. Confira o número e toque em ligar quando estiver pronto.")
            } else {
                openIntent(context, Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI), "Abrindo contatos para localizar $target. A ligação precisa ser confirmada por você.")
            }
        }

        val searchPrefix = Regex("^(pesquisar|pesquise|buscar na internet|procure na internet|google)\\s+")
        if (searchPrefix.containsMatchIn(q)) {
            val query = q.replaceFirst(searchPrefix, "").trim()
            if (query.isNotBlank()) return openIntent(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")), "Pesquisando na web por: $query")
        }

        val url = q.removePrefix("abrir site ").removePrefix("abre site ").removePrefix("acessar site ").trim()
        if ((q.startsWith("abrir site ") || q.startsWith("abre site ") || q.startsWith("acessar site ") || q.matches(Regex("(https?://)?[a-z0-9-]+(\\.[a-z0-9-]+)+(/.*)?"))) && url.contains('.')) {
            val address = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
            return openIntent(context, Intent(Intent.ACTION_VIEW, Uri.parse(address)), "Abrindo $address")
        }

        val open = Regex("^(por favor\\s+)?(abra|abrir|abre|inicie|iniciar|inicia|execute|executar|rode|rodar|lance|lancar|entre|entrar|acesse|acessar|mostre|mostrar)\\s+(o\\s+|a\\s+|os\\s+|as\\s+|no\\s+|na\\s+|meu\\s+|minha\\s+)?(aplicativo\\s+|app\\s+)?")
        if (open.containsMatchIn(q)) {
            val name = q.replace(open, "").replace(Regex("\\s+(agora|por favor|pra mim)$"), "").trim()
            val app = findApp(context, name)
            if (app != null && launchApp(context, app)) return "Abrindo ${app.label}."
            if (norm(name) in setOf("play store", "google play")) return storeSearch(context, "apps")
            return "Não encontrei um aplicativo instalado chamado ‘$name’. Diga ‘instalar $name’ para procurar na Play Store ou procure pelo nome na tela INÍCIO."
        }
        return null
    }

    private fun findApp(context: Context, query: String): LaunchableApp? {
        val q = norm(query).replace(Regex("^(o|a|os|as|do|da|no|na|meu|minha)\\s+"), "").trim()
        if (q.isBlank()) return null
        val synonyms = mapOf(
            "whats" to "whatsapp", "insta" to "instagram", "yt" to "youtube",
            "navegador" to "chrome browser firefox opera brave", "internet" to "chrome browser firefox opera brave",
            "fotos" to "photos galeria gallery", "galeria" to "photos galeria gallery",
            "mensagens" to "messages mensagens sms", "musica" to "spotify music youtube music",
            "mapas" to "maps mapas waze", "telefone" to "phone telefone dialer",
            "configuracoes" to "settings configuracoes", "camera" to "camera"
        )
        val terms = (synonyms[q] ?: q).split(' ')
        val apps = installedApps(context)
        return apps.firstOrNull { a -> terms.any { norm(a.label) == it || norm(a.packageName.substringAfterLast('.')) == it } }
            ?: apps.firstOrNull { a -> terms.any { t -> t.length >= 3 && (norm(a.label).contains(t) || norm(a.packageName).contains(t)) } }
    }

    private fun storeSearch(context: Context, query: String): String {
        return try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=${Uri.encode(query)}&c=apps")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            "Abri a busca por ‘$query’ na Play Store. Confira o aplicativo e toque em Instalar; o Android exige sua confirmação."
        } catch (_: Exception) {
            openIntent(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=${Uri.encode(query)}&c=apps")), "Abrindo a busca da Play Store na web. A instalação precisa ser confirmada por você.")
        }
    }

    private fun globalOrHint(action: String, success: String): String =
        if (JarvisAccessibilityService.globalAction(action)) success
        else "Para esse comando do sistema, abra AJUSTES > Controle do sistema (acessibilidade) e ative o serviço JARVIS manualmente."

    private fun openIntent(context: Context, intent: Intent, success: String): String = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        success
    } catch (_: Exception) {
        "Não consegui abrir essa tela neste aparelho. Verifique se existe um aplicativo compatível instalado."
    }

    private fun norm(text: String): String = Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "").replace(Regex("\\s+"), " ").trim()
}