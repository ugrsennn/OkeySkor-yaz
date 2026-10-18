package com.okey.skor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = GameStore(this)
        setContent {
            val dark = isSystemInDarkTheme()
            MaterialTheme(
                colorScheme = if (dark)
                    darkColorScheme(primary = Color(0xFF4FC3A1), secondary = Color(0xFFE57373))
                else
                    lightColorScheme(primary = Color(0xFF00796B), secondary = Color(0xFFC62828))
            ) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App(store)
                }
            }
        }
    }
}

@Composable
fun App(store: GameStore) {
    var game by remember { mutableStateOf(store.load()) }
    fun update(g: Game?) {
        game = g
        store.save(g)
    }

    val g = game
    if (g == null) {
        SetupScreen(onStart = { names, team -> update(Game(names, team, emptyList())) })
    } else {
        GameScreen(
            game = g,
            onChange = { update(it) },
            onNewGame = { keepNames ->
                update(if (keepNames) g.copy(rounds = emptyList()) else null)
            }
        )
    }
}

// ---------------- Oyun kurulumu ----------------

@Composable
fun SetupScreen(onStart: (List<String>, Boolean) -> Unit) {
    val names = remember { mutableStateListOf("", "", "", "") }
    var teamMode by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("101 Okey Skor", fontSize = 30.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text("Oyuncuların adını yaz, oyunu başlat.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))

        names.forEachIndexed { i, name ->
            OutlinedTextField(
                value = name,
                onValueChange = { names[i] = it.take(14) },
                label = {
                    Text(
                        if (teamMode) "Oyuncu ${i + 1} (Takım ${if (i % 2 == 0) 1 else 2})"
                        else "Oyuncu ${i + 1}"
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = teamMode, onCheckedChange = { teamMode = it })
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Eşli oyun", fontWeight = FontWeight.SemiBold)
                Text("Karşılıklı oturanlar eş: 1 & 3, 2 & 4", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val final = names.mapIndexed { i, n -> n.trim().ifEmpty { "Oyuncu ${i + 1}" } }
                onStart(final, teamMode)
            },
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) { Text("Oyunu Başlat", fontSize = 18.sp) }
    }
}

// ---------------- Oyun ekranı ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(game: Game, onChange: (Game) -> Unit, onNewGame: (Boolean) -> Unit) {
    var editingIndex by remember { mutableStateOf<Int?>(null) } // -1 = yeni el
    var askNewGame by remember { mutableStateOf(false) }
    val totals = game.totals()
    val minTotal = if (game.rounds.isEmpty()) null else totals.minOrNull()

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("101 Okey · ${game.rounds.size} el") },
                actions = {
                    IconButton(onClick = { askNewGame = true }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Yeni oyun")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editingIndex = -1 },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("El Ekle") }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            // Toplam puanlar
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                game.players.forEachIndexed { i, name ->
                    val leader = minTotal != null && totals[i] == minTotal
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (leader) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(name, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${totals[i]}", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                color = scoreColor(totals[i])
                            )
                            if (leader) Text("önde", fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            if (game.teamMode) {
                val (t1, t2) = game.teamTotals()
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TeamCard("${game.players[0]} & ${game.players[2]}", t1,
                        game.rounds.isNotEmpty() && t1 < t2, Modifier.weight(1f))
                    TeamCard("${game.players[1]} & ${game.players[3]}", t2,
                        game.rounds.isNotEmpty() && t2 < t1, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(8.dp))

            // El tablosu başlığı
            Row(
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 8.dp, horizontal = 8.dp)
            ) {
                Text("El", Modifier.width(36.dp), fontWeight = FontWeight.Bold)
                game.players.forEach {
                    Text(it, Modifier.weight(1f), textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            if (game.rounds.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Henüz el yok.\nHer el bitince \"El Ekle\"ye bas.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 90.dp)) {
                    itemsIndexed(game.rounds) { idx, row ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable { editingIndex = idx }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Text("${idx + 1}", Modifier.width(36.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            row.forEach { v ->
                                Text("$v", Modifier.weight(1f), textAlign = TextAlign.Center,
                                    color = scoreColor(v), fontSize = 16.sp)
                            }
                        }
                        HorizontalDivider()
                    }
                    item {
                        Text("Düzeltmek veya silmek için bir ele dokun.",
                            Modifier.fillMaxWidth().padding(12.dp), textAlign = TextAlign.Center,
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    editingIndex?.let { idx ->
        RoundDialog(
            players = game.players,
            initial = if (idx >= 0) game.rounds[idx] else null,
            title = if (idx >= 0) "${idx + 1}. eli düzenle" else "${game.rounds.size + 1}. el",
            onDismiss = { editingIndex = null },
            onSave = { values ->
                val newRounds = game.rounds.toMutableList()
                if (idx >= 0) newRounds[idx] = values else newRounds.add(values)
                onChange(game.copy(rounds = newRounds))
                editingIndex = null
            },
            onDelete = if (idx >= 0) ({
                onChange(game.copy(rounds = game.rounds.filterIndexed { i, _ -> i != idx }))
                editingIndex = null
            }) else null
        )
    }

    if (askNewGame) {
        AlertDialog(
            onDismissRequest = { askNewGame = false },
            title = { Text("Yeni oyun") },
            text = { Text("Mevcut skorlar silinecek. Aynı oyuncularla mı devam edilsin?") },
            confirmButton = {
                TextButton(onClick = { askNewGame = false; onNewGame(true) }) { Text("Aynı oyuncular") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { askNewGame = false; onNewGame(false) }) { Text("Yeni oyuncular") }
                    TextButton(onClick = { askNewGame = false }) { Text("Vazgeç") }
                }
            }
        )
    }
}

@Composable
fun TeamCard(label: String, total: Int, leader: Boolean, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (leader) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, Modifier.weight(1f), fontSize = 13.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Text("$total", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = scoreColor(total))
        }
    }
}

@Composable
fun scoreColor(v: Int): Color =
    if (v < 0) MaterialTheme.colorScheme.primary
    else if (v > 0) MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurfaceVariant

// ---------------- El girme penceresi ----------------

@Composable
fun RoundDialog(
    players: List<String>,
    initial: List<Int>?,
    title: String,
    onDismiss: () -> Unit,
    onSave: (List<Int>) -> Unit,
    onDelete: (() -> Unit)?
) {
    val values = remember {
        mutableStateListOf(*Array(players.size) { i -> initial?.get(i)?.toString() ?: "" })
    }
    var askDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Elinde kalan sayıları yaz. Biten oyuncu için hızlı düğmeleri kullan.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                players.forEachIndexed { i, name ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = values[i],
                                onValueChange = { s ->
                                    val clean = s.filterIndexed { k, c -> c.isDigit() || (c == '-' && k == 0) }
                                    values[i] = clean.take(5)
                                },
                                label = { Text(name) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = {
                                    val v = values[i]
                                    values[i] = if (v.startsWith("-")) v.drop(1) else "-$v"
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) { Text("±", fontSize = 18.sp) }
                        }
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            QuickChip("Bitti −101") { values[i] = "-101" }
                            QuickChip("Okeyle/Çifte −202") { values[i] = "-202" }
                            QuickChip("Açamadı 202") { values[i] = "202" }
                            QuickChip("Ceza +101") {
                                values[i] = ((values[i].toIntOrNull() ?: 0) + 101).toString()
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(values.map { if (it == "-") 0 else it.toIntOrNull() ?: 0 })
            }) { Text("Kaydet") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = { askDelete = true }) {
                        Text("Sil", color = MaterialTheme.colorScheme.secondary)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Vazgeç") }
            }
        }
    )

    if (askDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { askDelete = false },
            title = { Text("Eli sil") },
            text = { Text("Bu el silinsin mi?") },
            confirmButton = { TextButton(onClick = { askDelete = false; onDelete() }) { Text("Sil") } },
            dismissButton = { TextButton(onClick = { askDelete = false }) { Text("Vazgeç") } }
        )
    }
}

@Composable
fun QuickChip(label: String, onClick: () -> Unit) {
    AssistChip(onClick = onClick, label = { Text(label, fontSize = 12.sp) })
}
