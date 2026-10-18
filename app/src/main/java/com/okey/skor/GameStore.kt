package com.okey.skor

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Bir oyunun tüm durumu. rounds: her el için oyuncu sırasıyla puanlar. */
data class Game(
    val players: List<String>,
    val teamMode: Boolean,
    val rounds: List<List<Int>>
) {
    fun totals(): List<Int> = players.indices.map { i -> rounds.sumOf { it[i] } }

    /** Eşli modda takım 1 = oyuncu 1 ve 3, takım 2 = oyuncu 2 ve 4. */
    fun teamTotals(): Pair<Int, Int> {
        val t = totals()
        return (t[0] + t[2]) to (t[1] + t[3])
    }
}

/** Oyunu telefonda saklar; uygulama kapansa bile skorlar kaybolmaz. */
class GameStore(context: Context) {
    private val prefs = context.getSharedPreferences("okey_skor", Context.MODE_PRIVATE)

    fun load(): Game? {
        val raw = prefs.getString("game", null) ?: return null
        return try {
            val o = JSONObject(raw)
            val p = o.getJSONArray("players")
            val r = o.getJSONArray("rounds")
            Game(
                players = List(p.length()) { p.getString(it) },
                teamMode = o.optBoolean("teamMode", false),
                rounds = List(r.length()) { i ->
                    val row = r.getJSONArray(i)
                    List(row.length()) { row.getInt(it) }
                }
            )
        } catch (e: Exception) {
            null
        }
    }

    fun save(game: Game?) {
        if (game == null) {
            prefs.edit().remove("game").apply()
            return
        }
        val o = JSONObject()
        o.put("players", JSONArray(game.players))
        o.put("teamMode", game.teamMode)
        o.put("rounds", JSONArray(game.rounds.map { JSONArray(it) }))
        prefs.edit().putString("game", o.toString()).apply()
    }
}
