package com.cris.jarvis.ui

import android.content.Intent
import android.provider.Settings
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.cos
import kotlin.math.sin

private val HoloBg = Color(0xFF020913)
private val HoloPanel = Color(0xB8071C2B)

@Composable
fun LauncherScreen() {
    val context = LocalContext.current
    var apps by remember { mutableStateOf(DeviceCommandExecutor.installedApps(context)) }
    var filter by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("SISTEMA ONLINE • AGUARDANDO COMANDO") }
    var refresh by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        apps = DeviceCommandExecutor.installedApps(context)
        refresh++
    }
    val shown = remember(apps, filter, refresh) {
        if (filter.isBlank()) apps else apps.filter { it.label.contains(filter.trim(), true) || it.packageName.contains(filter.trim(), true) }
    }
    Box(Modifier.fillMaxSize().background(HoloBg)) {
        HolographicBackdrop()
        Column(Modifier.fillMaxSize().padding(horizontal = 13.dp, vertical = 9.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("J.A.R.V.I.S.", color = Neon.Ice, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 26.sp, letterSpacing = 3.2.sp)
                    Text("JUST A RATHER VERY INTELLIGENT SYSTEM", color = Neon.Cyan, fontSize = 7.sp, letterSpacing = .65.sp, fontFamily = FontFamily.Monospace)
                    Text("MOBILE HOLOGRAPHIC INTERFACE  /  V 04.0", color = Neon.Dim, fontSize = 7.sp, letterSpacing = .9.sp, fontFamily = FontFamily.Monospace)
                }
                Box(Modifier.size(65.dp), contentAlignment = Alignment.Center) {
                    ReactorCore(active = true, tint = Neon.Cyan, modifier = Modifier.size(61.dp))
                    Text("AI", color = Neon.Ice, fontFamily = FontFamily.Monospace, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            HoloStatusPanel(status = status, appCount = shown.size)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("▸  HOLOGRAPHIC APP GRID", color = Neon.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, fontFamily = FontFamily.Monospace)
                    Text("TODOS OS APLICATIVOS DISPONÍVEIS NO DISPOSITIVO", color = Neon.Dim, fontSize = 7.sp, letterSpacing = .3.sp)
                }
                TextButton(onClick = { apps = DeviceCommandExecutor.installedApps(context); filter = ""; status = "SCAN COMPLETO • LISTA ATUALIZADA" }) {
                    Text("⟳ SCAN", color = Neon.Cyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
            OutlinedTextField(value = filter, onValueChange = { filter = it }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                placeholder = { Text("PESQUISAR NOS APPS…", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                leadingIcon = { Text("⌕", color = Neon.Cyan, fontSize = 23.sp) })
            if (shown.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NENHUM APP VISÍVEL", color = Neon.Cyan, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        Text("Toque em SCAN ou tente outro nome.", color = Neon.Dim, fontSize = 10.sp)
                    }
                }
            } else {
                LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 70.dp), modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 5.dp, horizontal = 1.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(shown, key = { it.packageName }) { app ->
                        AnimatedVisibility(visible = true, enter = fadeIn(tween(450)) + scaleIn(initialScale = .88f, animationSpec = tween(450))) {
                            HoloAppTile(label = app.label, packageName = app.packageName, icon = app.icon) {
                                status = if (DeviceCommandExecutor.launchApp(context, app)) "EXECUTANDO • ${app.label.uppercase()}" else "FALHA AO ABRIR • ${app.label.uppercase()}"
                            }
                        }
                    }
                }
            }
            HoloCommandBar(
                onSettings = { try { context.startActivity(Intent(Settings.ACTION_SETTINGS)) } catch (_: Exception) { status = "CONFIGURAÇÕES INDISPONÍVEIS" } },
                onCamera = { status = DeviceCommandExecutor.execute(context, "tirar foto") ?: "CÂMERA NÃO ENCONTRADA" },
                onStore = { status = DeviceCommandExecutor.execute(context, "abrir play store") ?: "PLAY STORE NÃO ENCONTRADA" },
                onHome = { try { context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) } catch (_: Exception) { status = "Escolha JARVIS em Apps padrão > App de início." } }
            )
        }
    }
}

@Composable
private fun HolographicBackdrop() {
    val transition = rememberInfiniteTransition(label = "holo-backdrop")
    val rotation by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(18000, easing = FastOutSlowInEasing)), label = "orbit-rotation")
    val sweep by transition.animateFloat(-1f, 1f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scanner-sweep")
    val pulse by transition.animateFloat(.28f, .8f, infiniteRepeatable(tween(1900), RepeatMode.Reverse), label = "pulse")
    Canvas(Modifier.fillMaxSize()) {
        val cyan = Neon.Cyan.copy(alpha = .15f)
        val cx = size.width * .5f
        val cy = size.height * .36f
        val radius = size.minDimension * .40f
        for (i in 1..5) {
            drawCircle(cyan.copy(alpha = .035f + pulse * .025f), radius = radius * i / 5f, center = androidx.compose.ui.geometry.Offset(cx, cy), style = Stroke(width = 1f))
        }
        for (i in 0 until 36) {
            val a = Math.toRadians((i * 10f + rotation).toDouble())
            val inner = radius * .76f
            val outer = if (i % 3 == 0) radius * .98f else radius * .91f
            drawLine(cyan.copy(alpha = if (i % 3 == 0) .7f else .28f),
                androidx.compose.ui.geometry.Offset(cx + cos(a).toFloat() * inner, cy + sin(a).toFloat() * inner),
                androidx.compose.ui.geometry.Offset(cx + cos(a).toFloat() * outer, cy + sin(a).toFloat() * outer), strokeWidth = if (i % 3 == 0) 2f else 1f)
        }
        drawArc(Neon.Cyan.copy(alpha = .35f), startAngle = rotation, sweepAngle = 84f, useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(cx - radius, cy - radius), size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2), style = Stroke(width = 2f, cap = StrokeCap.Round))
        val scanY = size.height * (.16f + (sweep + 1f) * .34f)
        drawLine(Neon.Cyan.copy(alpha = .13f), androidx.compose.ui.geometry.Offset(0f, scanY), androidx.compose.ui.geometry.Offset(size.width, scanY), strokeWidth = 1f)
        // HUD grid lines and tiny telemetry lights.
        val step = 34.dp.toPx()
        var x = 0f
        while (x < size.width) { drawLine(Color(0xFF174A60).copy(alpha = .12f), androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), strokeWidth = .6f); x += step }
        var y = 0f
        while (y < size.height) { drawLine(Color(0xFF174A60).copy(alpha = .10f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = .6f); y += step }
        for (i in 0 until 18) {
            val px = (i * 73f + 17f) % size.width
            val py = (i * 127f + 31f) % size.height
            drawCircle(Neon.Cyan.copy(alpha = pulse * .42f), radius = if (i % 4 == 0) 2.4f else 1.2f, center = androidx.compose.ui.geometry.Offset(px, py))
        }
    }
}

@Composable
private fun HoloStatusPanel(status: String, appCount: Int) {
    val transition = rememberInfiniteTransition(label = "status")
    val blink by transition.animateFloat(.35f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "status-blink")
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(HoloPanel)
        .border(1.dp, Neon.Cyan.copy(alpha = .55f), RoundedCornerShape(14.dp)).padding(11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).alpha(blink).clip(CircleShape).background(Neon.Green))
            Spacer(Modifier.size(6.dp))
            Text("ARC REACTOR  /  CORE ONLINE", color = Neon.Cyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = .7.sp)
            Spacer(Modifier.weight(1f))
            Text("LIVE", color = Neon.Green, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.height(7.dp))
        Text(status, color = Neon.Ice, fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
            TelemetryCell("APP INDEX", appCount.toString().padStart(2, '0'), Modifier.weight(1f))
            TelemetryCell("INTERFACE", "HOLOGRAPHIC", Modifier.weight(1f))
            TelemetryCell("NETWORK", "STANDBY", Modifier.weight(1f))
        }
    }
}

@Composable
private fun TelemetryCell(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(7.dp)).background(Color(0x5521BDE0)).padding(horizontal = 7.dp, vertical = 6.dp)) {
        Text(title, color = Neon.Dim, fontSize = 6.sp, letterSpacing = .6.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(3.dp))
        Text(value, color = Neon.Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Clip)
    }
}

@Composable
private fun HoloAppTile(label: String, packageName: String, icon: android.graphics.drawable.Drawable, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "tile-$packageName")
    val pulse by transition.animateFloat(.35f, .8f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "tile-pulse")
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
        .background(Color(0xA6071B2A)).border(1.dp, Neon.Cyan.copy(alpha = pulse * .42f), RoundedCornerShape(13.dp))
        .clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF071421)).border(1.dp, Neon.Blue.copy(alpha = .7f), CircleShape), contentAlignment = Alignment.Center) {
            val painter = remember(packageName) { BitmapPainter(icon.toBitmap(96, 96).asImageBitmap()) }
            Image(painter = painter, contentDescription = label, modifier = Modifier.size(31.dp))
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Neon.Cyan.copy(alpha = pulse * .25f), style = Stroke(width = 1.dp.toPx()))
            }
        }
        Text(label, color = Neon.Text, fontSize = 9.sp, lineHeight = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("OPEN ↗", color = Neon.Cyan.copy(alpha = pulse), fontSize = 6.sp, fontFamily = FontFamily.Monospace, letterSpacing = .5.sp)
    }
}

@Composable
private fun HoloCommandBar(onSettings: () -> Unit, onCamera: () -> Unit, onStore: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xE9061421))
        .border(1.dp, Neon.Blue.copy(alpha = .55f), RoundedCornerShape(14.dp)).padding(9.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("SYSTEM SHORTCUTS", color = Neon.Cyan, fontSize = 8.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.1.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("TOUCH INTERFACE  •  READY", color = Neon.Dim, fontSize = 6.sp, fontFamily = FontFamily.Monospace)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            HoloButton("⚙", "SYSTEM", Modifier.weight(1f), onSettings)
            HoloButton("◉", "CAMERA", Modifier.weight(1f), onCamera)
            HoloButton("↗", "STORE", Modifier.weight(1f), onStore)
        }
        Button(onClick = onHome, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18C8E8), contentColor = Color(0xFF00111A)),
            shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
            Text("⌂   SET J.A.R.V.I.S. AS HOME SYSTEM", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .4.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun HoloButton(symbol: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(8.dp)).background(Color(0x66217B9A))
        .border(1.dp, Neon.Cyan.copy(alpha = .35f), RoundedCornerShape(8.dp)).clickable(onClick = onClick)
        .padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(symbol, color = Neon.Ice, fontSize = 17.sp)
        Text(label, color = Neon.Cyan, fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = .7.sp, fontFamily = FontFamily.Monospace)
    }
}
