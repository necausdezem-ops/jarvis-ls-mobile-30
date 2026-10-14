package com.cris.jarvis.ui

import android.content.Intent
import android.provider.Settings
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.cris.jarvis.actions.DeviceCommandExecutor

@Composable
fun LauncherScreen() {
    val context = LocalContext.current
    var apps by remember { mutableStateOf(DeviceCommandExecutor.installedApps(context)) }
    var filter by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("INTERFACE LOCAL • PRONTA") }
    var refresh by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        apps = DeviceCommandExecutor.installedApps(context)
        refresh++
    }
    val shown = remember(apps, filter, refresh) {
        if (filter.isBlank()) apps else apps.filter { it.label.contains(filter.trim(), true) || it.packageName.contains(filter.trim(), true) }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("J.A.R.V.I.S.", color = Neon.Ice, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 24.sp, letterSpacing = 3.sp)
                Text("PERSONAL MOBILE INTERFACE", color = Neon.Dim, fontSize = 8.sp, letterSpacing = 1.3.sp, fontFamily = FontFamily.Monospace)
            }
            Box(Modifier.size(47.dp).clip(CircleShape).background(Color(0xFF071A27)).border(1.dp, Neon.Cyan.copy(alpha = .7f), CircleShape), contentAlignment = Alignment.Center) {
                ReactorCore(active = true, tint = Neon.Cyan, modifier = Modifier.size(45.dp))
            }
        }
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("HUD • HOME SYSTEM", color = Neon.Cyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(status, color = Neon.Dim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                Text("${shown.size.toString().padStart(2, '0')} APPS", color = Neon.Green, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAction("⚙", "AJUSTES", Modifier.weight(1f)) {
                    try { context.startActivity(Intent(Settings.ACTION_SETTINGS)) } catch (_: Exception) { status = "Não foi possível abrir Configurações." }
                }
                QuickAction("◉", "CÂMERA", Modifier.weight(1f)) {
                    status = DeviceCommandExecutor.execute(context, "tirar foto") ?: "Procure Câmera na lista de aplicativos."
                }
                QuickAction("↗", "PLAY STORE", Modifier.weight(1f)) {
                    status = DeviceCommandExecutor.execute(context, "abrir play store") ?: "Play Store não encontrada."
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { Title("APLICATIVOS INSTALADOS") }
            TextButton(onClick = { apps = DeviceCommandExecutor.installedApps(context); filter = ""; status = "LISTA ATUALIZADA" }) {
                Text("ATUALIZAR", color = Neon.Cyan, fontSize = 9.sp)
            }
        }
        OutlinedTextField(value = filter, onValueChange = { filter = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(10.dp), placeholder = { Text("Buscar um aplicativo…", fontSize = 12.sp) },
            leadingIcon = { Text("⌕", color = Neon.Cyan, fontSize = 22.sp) })
        if (shown.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NENHUM APP ENCONTRADO", color = Neon.Cyan, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Tente outra busca ou use ‘instalar nome do app’.", color = Neon.Dim, fontSize = 11.sp)
                }
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(shown, key = { it.packageName }) { app ->
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0x5B0B2131))
                        .border(1.dp, Neon.Cyan.copy(alpha = .12f), RoundedCornerShape(12.dp))
                        .clickable { status = if (DeviceCommandExecutor.launchApp(context, app)) "ABRINDO • ${app.label.uppercase()}" else "Não consegui abrir ${app.label}." }
                        .padding(horizontal = 3.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val painter = remember(app.packageName) { BitmapPainter(app.icon.toBitmap(96, 96).asImageBitmap()) }
                        Image(painter = painter, contentDescription = app.label, modifier = Modifier.size(38.dp))
                        Text(app.label, color = Neon.Text, fontSize = 9.sp, lineHeight = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        Panel {
            Text("COMANDO RÁPIDO", color = Neon.Cyan, fontSize = 9.sp, letterSpacing = 1.3.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text("Na aba NÚCLEO, diga ou digite: ‘abrir WhatsApp’, ‘pesquisar previsão do tempo’ ou ‘instalar Spotify’.", color = Neon.Dim, fontSize = 11.sp, lineHeight = 16.sp)
            Spacer(Modifier.height(5.dp))
            Button(onClick = {
                try { context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
                catch (_: Exception) { status = "Abra Configurações > Apps padrão > App de início e escolha JARVIS." }
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Neon.Cyan, contentColor = Color(0xFF001017)),
                shape = RoundedCornerShape(9.dp)) {
                Text("DEFINIR JARVIS COMO TELA INICIAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .3.sp)
            }
        }
    }
}

@Composable
private fun QuickAction(symbol: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(8.dp)).background(Color(0x55338DFF)).border(1.dp, Neon.Blue.copy(alpha = .35f), RoundedCornerShape(8.dp))
        .clickable(onClick = onClick).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(symbol, color = Neon.Cyan, fontSize = 17.sp)
        Text(label, color = Neon.Text, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .3.sp)
    }
}