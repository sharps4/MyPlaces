package com.example.myplaces

import com.example.myplaces.data.json.ExportFile
import com.example.myplaces.data.json.PlaceJson
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Vérifie la stabilité du format d'échange `places_export.json` : c'est le contrat
 * entre deux installations de l'application.
 */
class ExportFormatTest {

    private val gson = Gson()

    private fun place(uuid: String) = PlaceJson(
        uuid = uuid,
        title = "Pont Neuf",
        description = "Premier café en terrasse",
        emoji = "☕",
        latitude = 48.8566,
        longitude = 2.3522,
        address = "1 Quai du Louvre 75001 Paris",
        createdAt = 1_700_000_000_000L
    )

    @Test
    fun `un export se relit a l identique`() {
        val original = ExportFile(
            exportedAt = 1_700_000_000_000L,
            author = "adrian",
            places = listOf(place("uuid-1"), place("uuid-2"))
        )

        val roundTripped = gson.fromJson(gson.toJson(original), ExportFile::class.java)

        assertEquals(original, roundTripped)
        assertEquals(ExportFile.FORMAT_VERSION, roundTripped.formatVersion)
    }

    @Test
    fun `la photo est absente du json quand le lieu n en a pas`() {
        val json = gson.toJson(place("uuid-1"))

        assertTrue(json.contains("\"uuid\""))
        assertNull(gson.fromJson(json, PlaceJson::class.java).photoBase64)
    }

    @Test
    fun `un fichier sans liste de lieux reste exploitable`() {
        val minimal = """{"formatVersion":1,"exportedAt":0,"author":"ami"}"""

        val parsed = gson.fromJson(minimal, ExportFile::class.java)

        assertEquals("ami", parsed.author)
        assertTrue(parsed.places.isEmpty())
    }

    @Test
    fun `la deduplication se fait sur l uuid`() {
        val known = setOf("uuid-1")
        val incoming = listOf(place("uuid-1"), place("uuid-2"))

        val toInsert = incoming.filterNot { it.uuid in known }

        assertEquals(1, toInsert.size)
        assertEquals("uuid-2", toInsert.first().uuid)
    }
}
