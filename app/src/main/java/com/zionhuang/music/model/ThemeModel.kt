/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.zionhuang.music.model

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject

@Immutable
@Serializable
data class ThemeCategory(
    val id: String,
    val title: String,
    val icon: String // Material icon name or emoji
)

@Immutable
@Serializable
data class CuratedTheme(
    val id: String,
    val name: String,
    val categoryId: String,
    val primaryHex: String,
    val secondaryHex: String,
    val backgroundHex: String,
    val surfaceHex: String,
    val isDark: Boolean = true
) {
    val primaryColor: Color
        get() = parseHexColor(primaryHex)

    val secondaryColor: Color
        get() = parseHexColor(secondaryHex)

    val backgroundColor: Color
        get() = parseHexColor(backgroundHex)

    val surfaceColor: Color
        get() = parseHexColor(surfaceHex)

    companion object {
        fun parseHexColor(hex: String): Color {
            val cleanHex = hex.removePrefix("#")
            val colorInt = when (cleanHex.length) {
                6 -> 0xFF000000.toInt() or cleanHex.toLong(16).toInt()
                8 -> cleanHex.toLong(16).toInt()
                else -> 0xFF000000.toInt()
            }
            return Color(colorInt)
        }
    }
}

@Immutable
@Serializable
data class ThemeCatalog(
    val version: Int = 1,
    val categories: List<ThemeCategory> = emptyList(),
    val themes: List<CuratedTheme> = emptyList()
) {
    companion object {
        @Volatile
        private var cachedCatalog: ThemeCatalog? = null

        fun loadFromAssets(context: Context): ThemeCatalog {
            cachedCatalog?.let { return it }
            synchronized(this) {
                cachedCatalog?.let { return it }
                val catalog = try {
                    val jsonString = context.assets.open("themes_catalog.json").bufferedReader().use { it.readText() }
                    val json = Json { ignoreUnknownKeys = true }
                    json.decodeFromString<ThemeCatalog>(jsonString)
                } catch (e: Exception) {
                    try {
                        val jsonString = context.assets.open("themes_catalog.json").bufferedReader().use { it.readText() }
                        val root = JSONObject(jsonString)
                        val categoriesArray = root.optJSONArray("categories") ?: JSONArray()
                        val categoriesList = ArrayList<ThemeCategory>(categoriesArray.length())
                        for (i in 0 until categoriesArray.length()) {
                            val obj = categoriesArray.getJSONObject(i)
                            categoriesList.add(
                                ThemeCategory(
                                    id = obj.getString("id"),
                                    title = obj.getString("title"),
                                    icon = obj.optString("icon", "🎨")
                                )
                            )
                        }

                        val themesArray = root.optJSONArray("themes") ?: JSONArray()
                        val themesList = ArrayList<CuratedTheme>(themesArray.length())
                        for (i in 0 until themesArray.length()) {
                            val obj = themesArray.getJSONObject(i)
                            themesList.add(
                                CuratedTheme(
                                    id = obj.getString("id"),
                                    name = obj.getString("name"),
                                    categoryId = obj.getString("categoryId"),
                                    primaryHex = obj.getString("primaryHex"),
                                    secondaryHex = obj.getString("secondaryHex"),
                                    backgroundHex = obj.getString("backgroundHex"),
                                    surfaceHex = obj.getString("surfaceHex"),
                                    isDark = obj.optBoolean("isDark", true)
                                )
                            )
                        }
                        ThemeCatalog(
                            version = root.optInt("version", 1),
                            categories = categoriesList,
                            themes = themesList
                        )
                    } catch (e2: Exception) {
                        ThemeCatalog()
                    }
                }
                cachedCatalog = catalog
                return catalog
            }
        }
    }
}
