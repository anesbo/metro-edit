/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.settings

typealias ThemeCatalogViewModel = com.zionhuang.music.ui.screens.settings.ThemeCatalogViewModel

@androidx.compose.runtime.Composable
fun ThemeCatalogScreen(
    navController: androidx.navigation.NavController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    viewModel: ThemeCatalogViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    com.zionhuang.music.ui.screens.settings.ThemeCatalogScreen(
        navController = navController,
        modifier = modifier,
        viewModel = viewModel,
    )
}
