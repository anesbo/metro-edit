/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.zionhuang.music.data

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import com.metrolist.music.constants.ActiveThemeIdDefault
import com.metrolist.music.constants.ActiveThemeIdKey
import com.metrolist.music.constants.BackgroundStyle
import com.metrolist.music.constants.BackgroundStyleKey
import com.metrolist.music.constants.CustomBgColorKey
import com.metrolist.music.constants.CustomPrimaryColorKey
import com.metrolist.music.constants.CustomSecondaryColorKey
import com.metrolist.music.constants.DarkModeKey
import com.metrolist.music.constants.GradientEndColorKey
import com.metrolist.music.constants.GradientStartColorKey
import com.metrolist.music.constants.UseCustomAccentKey
import com.metrolist.music.ui.screens.settings.DarkMode
import com.metrolist.music.utils.dataStore
import com.metrolist.music.utils.safeDataStoreEdit
import com.zionhuang.music.model.CuratedTheme
import com.zionhuang.music.model.ThemeCategory
import com.zionhuang.music.model.ThemeCatalog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _catalog = MutableStateFlow(ThemeCatalog())
    val catalog: StateFlow<ThemeCatalog> = _catalog.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDarkFilter = MutableStateFlow<Boolean?>(null)
    val isDarkFilter: StateFlow<Boolean?> = _isDarkFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val categories: StateFlow<List<ThemeCategory>> =
        _catalog
            .map { it.categories }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val allThemes: StateFlow<List<CuratedTheme>> =
        _catalog
            .map { it.themes }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val themes: StateFlow<List<CuratedTheme>> =
        combine(
            _catalog,
            _selectedCategory,
            _searchQuery,
            _isDarkFilter,
        ) { cat, selectedCat, query, darkFilter ->
            val trimmedQuery = query.trim().lowercase()
            val normalizedCategory = selectedCat.trim().lowercase()

            cat.themes.filter { theme ->
                // Category filter
                val matchesCategory =
                    if (normalizedCategory == "all" || normalizedCategory.isBlank()) {
                        true
                    } else {
                        val themeCat = theme.categoryId.lowercase()
                        themeCat == normalizedCategory ||
                            themeCat.contains(normalizedCategory) ||
                            normalizedCategory.contains(themeCat) ||
                            themeCat.replace("_", "").contains(normalizedCategory.replace("_", ""))
                    }

                // Dark/Light filter
                val matchesDark = darkFilter == null || theme.isDark == darkFilter

                // Search query filter
                val matchesQuery =
                    if (trimmedQuery.isBlank()) {
                        true
                    } else {
                        theme.name.lowercase().contains(trimmedQuery) ||
                            theme.id.lowercase().contains(trimmedQuery) ||
                            theme.primaryHex.lowercase().contains(trimmedQuery) ||
                            theme.secondaryHex.lowercase().contains(trimmedQuery) ||
                            theme.backgroundHex.lowercase().contains(trimmedQuery) ||
                            theme.surfaceHex.lowercase().contains(trimmedQuery)
                    }

                matchesCategory && matchesDark && matchesQuery
            }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        loadThemesAsync()
    }

    fun loadThemesAsync() {
        scope.launch {
            _isLoading.value = true
            val loaded =
                withContext(Dispatchers.IO) {
                    ThemeCatalog.loadFromAssets(context)
                }
            _catalog.value = loaded
            _isLoading.value = false
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDarkFilter(isDark: Boolean?) {
        _isDarkFilter.value = isDark
    }

    fun resetFilters() {
        _selectedCategory.value = "ALL"
        _searchQuery.value = ""
        _isDarkFilter.value = null
    }

    fun getActiveThemeId(): Flow<String> {
        return context.dataStore.data
            .map { prefs ->
                prefs[ActiveThemeIdKey] ?: ActiveThemeIdDefault
            }.distinctUntilChanged()
    }

    fun getActiveTheme(): Flow<CuratedTheme?> {
        return combine(getActiveThemeId(), _catalog) { activeId, cat ->
            if (activeId.isBlank()) {
                null
            } else {
                cat.themes.firstOrNull { it.id == activeId }
            }
        }
    }

    fun applyTheme(theme: CuratedTheme) {
        scope.launch(Dispatchers.IO) {
            applyThemeSuspending(theme)
        }
    }

    suspend fun applyThemeSuspending(theme: CuratedTheme): Boolean {
        return context.safeDataStoreEdit { settings ->
            settings[ActiveThemeIdKey] = theme.id
            settings[UseCustomAccentKey] = true
            settings[CustomPrimaryColorKey] = theme.primaryColor.toArgb()
            settings[CustomSecondaryColorKey] = theme.secondaryColor.toArgb()
            settings[BackgroundStyleKey] = BackgroundStyle.SOLID_CUSTOM.name
            settings[CustomBgColorKey] = theme.backgroundColor.toArgb()
            settings[GradientStartColorKey] = theme.primaryColor.toArgb()
            settings[GradientEndColorKey] = theme.backgroundColor.toArgb()
            settings[DarkModeKey] = if (theme.isDark) DarkMode.ON.name else DarkMode.OFF.name
        }
    }

    companion object {
        @Volatile
        private var instance: ThemeRepository? = null

        fun getInstance(context: Context): ThemeRepository {
            return instance ?: synchronized(this) {
                instance ?: ThemeRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
