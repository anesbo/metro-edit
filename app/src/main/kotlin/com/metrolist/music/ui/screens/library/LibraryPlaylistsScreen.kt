/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.metrolist.innertube.utils.parseCookieString
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.R
import com.metrolist.music.constants.CONTENT_TYPE_HEADER
import com.metrolist.music.constants.CONTENT_TYPE_PLAYLIST
import com.metrolist.music.constants.GridItemSize
import com.metrolist.music.constants.GridItemsSizeKey
import com.metrolist.music.constants.GridThumbnailHeight
import com.metrolist.music.constants.InnerTubeCookieKey
import com.metrolist.music.constants.LibraryViewType
import com.metrolist.music.constants.PlaylistSortDescendingKey
import com.metrolist.music.constants.PlaylistSortType
import com.metrolist.music.constants.PlaylistSortTypeKey
import com.metrolist.music.constants.ShowCachedPlaylistKey
import com.metrolist.music.constants.ShowDownloadedPlaylistKey
import com.metrolist.music.constants.ShowLikedPlaylistKey
import com.metrolist.music.constants.ShowTopPlaylistKey
import com.metrolist.music.constants.ShowUploadedPlaylistKey
import com.metrolist.music.constants.YtmSyncKey
import com.metrolist.music.db.entities.Playlist
import com.metrolist.music.db.entities.PlaylistEntity
import com.metrolist.music.extensions.matchesNormalizedQuery
import com.metrolist.music.extensions.normalizeForSearch
import com.metrolist.music.ui.component.CreatePlaylistDialog
import com.metrolist.music.ui.component.LibraryPlaylistGridItem
import com.metrolist.music.ui.component.LibraryPlaylistListItem
import com.metrolist.music.ui.component.LibrarySearchEmptyPlaceholder
import com.metrolist.music.ui.component.LibrarySearchHeader
import com.metrolist.music.ui.component.LocalMenuState
import com.metrolist.music.ui.component.PlaylistGridItem
import com.metrolist.music.ui.component.PlaylistListItem
import com.metrolist.music.ui.component.SortHeader
import com.metrolist.music.utils.rememberEnumPreference
import com.metrolist.music.utils.rememberPreference
import com.metrolist.music.viewmodels.LibraryPlaylistsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.util.UUID

private data class VisiblePlaylistItem(
    val key: String,
    val playlist: Playlist,
    val autoPlaylist: Boolean,
    val route: String? = null,
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryPlaylistsScreen(
    navController: NavController,
    filterContent: @Composable () -> Unit,
    viewType: LibraryViewType,
    onViewTypeChange: (LibraryViewType) -> Unit,
    viewModel: LibraryPlaylistsViewModel = hiltViewModel(),
    initialTextFieldValue: String? = null,
    allowSyncing: Boolean = true,
) {
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val coroutineScope = rememberCoroutineScope()

    val (sortType, onSortTypeChange) = rememberEnumPreference(
        PlaylistSortTypeKey,
        PlaylistSortType.CREATE_DATE
    )
    val (sortDescending, onSortDescendingChange) = rememberPreference(
        PlaylistSortDescendingKey,
        true
    )
    val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)

    val playlists by viewModel.allPlaylists.collectAsStateWithLifecycle()

    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val normalizedQuery = remember(searchQuery) { searchQuery.normalizeForSearch() }
    val filteredPlaylists = remember(playlists, normalizedQuery) {
        if (normalizedQuery.isBlank()) playlists
        else playlists.filter { matchesNormalizedQuery(normalizedQuery, it.playlist.name) }
    }

    val topSize by viewModel.topValue.collectAsStateWithLifecycle(initialValue = 50)

    // ── Shortcut playlists ───────────────────────────────────────────────
    val likedPlaylist = Playlist(
        playlist = PlaylistEntity(id = UUID.randomUUID().toString(), name = stringResource(R.string.liked)),
        songCount = 0, songThumbnails = emptyList(),
    )
    val downloadPlaylist = Playlist(
        playlist = PlaylistEntity(id = UUID.randomUUID().toString(), name = stringResource(R.string.offline)),
        songCount = 0, songThumbnails = emptyList(),
    )
    val topPlaylist = Playlist(
        playlist = PlaylistEntity(id = UUID.randomUUID().toString(), name = stringResource(R.string.my_top) + " $topSize"),
        songCount = 0, songThumbnails = emptyList(),
    )
    val uploadedPlaylist = Playlist(
        playlist = PlaylistEntity(id = UUID.randomUUID().toString(), name = stringResource(R.string.uploaded_playlist)),
        songCount = 0, songThumbnails = emptyList(),
    )
    val cachedPlaylist = Playlist(
        playlist = PlaylistEntity(id = UUID.randomUUID().toString(), name = stringResource(R.string.cached_playlist)),
        songCount = 0, songThumbnails = emptyList(),
    )

    val (showLiked, onShowLikedChange) = rememberPreference(ShowLikedPlaylistKey, true)
    val (showDownloaded, onShowDownloadedChange) = rememberPreference(ShowDownloadedPlaylistKey, true)
    val (showTop, onShowTopChange) = rememberPreference(ShowTopPlaylistKey, true)
    val (showUploaded, onShowUploadedChange) = rememberPreference(ShowUploadedPlaylistKey, true)
    val (showCached, onShowCachedChange) = rememberPreference(ShowCachedPlaylistKey, true)

    val showLikedPlaylist = showLiked && matchesNormalizedQuery(normalizedQuery, likedPlaylist.playlist.name)
    val showDownloadedPlaylist = showDownloaded && matchesNormalizedQuery(normalizedQuery, downloadPlaylist.playlist.name)
    val showCachedPlaylists = showCached && matchesNormalizedQuery(normalizedQuery, cachedPlaylist.playlist.name)
    val showTopPlaylists = showTop && matchesNormalizedQuery(normalizedQuery, topPlaylist.playlist.name)
    val showUploadedPlaylists = showUploaded && matchesNormalizedQuery(normalizedQuery, uploadedPlaylist.playlist.name)

    val autoItems = remember(
        showLikedPlaylist, showDownloadedPlaylist, showCachedPlaylists, showTopPlaylists,
        showUploadedPlaylists, topSize,
    ) {
        buildList {
            if (showLikedPlaylist) add(VisiblePlaylistItem("likedPlaylist", likedPlaylist, true, "auto_playlist/liked"))
            if (showDownloadedPlaylist) add(VisiblePlaylistItem("downloadedPlaylist", downloadPlaylist, true, "auto_playlist/downloaded"))
            if (showCachedPlaylists) add(VisiblePlaylistItem("cachedPlaylist", cachedPlaylist, true, "cache_playlist/cached"))
            if (showTopPlaylists) add(VisiblePlaylistItem("TopPlaylist", topPlaylist, true, "top_playlist/$topSize"))
            if (showUploadedPlaylists) add(VisiblePlaylistItem("uploadedPlaylist", uploadedPlaylist, true, "auto_playlist/uploaded"))
        }
    }

    // ── Drag-and-drop state ──────────────────────────────────────────────
    // reorderMode is only relevant when sortType == CUSTOM
    var reorderMode by rememberSaveable { mutableStateOf(false) }
    val mutablePlaylists = remember { mutableStateListOf<VisiblePlaylistItem>() }

    // Sync mutablePlaylists with DB-derived list when not actively dragging
    val userItems = remember(filteredPlaylists) {
        filteredPlaylists.distinctBy { it.id }.map { VisiblePlaylistItem(it.id, it, false) }
    }
    LaunchedEffect(userItems) {
        if (mutablePlaylists.isEmpty() || !reorderMode) {
            mutablePlaylists.clear()
            mutablePlaylists.addAll(userItems)
        }
    }

    val visibleResults = remember(autoItems, mutablePlaylists) {
        autoItems + mutablePlaylists
    }

    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()
    val headerItems = 2 // filter + header items before playlist items

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        scrollThresholdPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
    ) { from, to ->
        val autoCount = autoItems.size
        val fromIdx = from.index - headerItems - autoCount
        val toIdx = to.index - headerItems - autoCount
        if (fromIdx >= 0 && toIdx >= 0) {
            mutablePlaylists.add(toIdx, mutablePlaylists.removeAt(fromIdx))
        }
    }

    // Persist order after drag ends
    LaunchedEffect(reorderableState.isAnyItemDragging) {
        if (!reorderableState.isAnyItemDragging && sortType == PlaylistSortType.CUSTOM) {
            viewModel.saveCustomOrder(mutablePlaylists.map { it.key })
        }
    }

    // ── Shortcut editor bottom sheet ─────────────────────────────────────
    var showShortcutEditor by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop =
        backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsStateWithLifecycle()

    val (innerTubeCookie) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }
    val (ytmSync) = rememberPreference(YtmSyncKey, true)

    LaunchedEffect(Unit) {
        if (ytmSync) withContext(Dispatchers.IO) { viewModel.sync() }
    }

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            when (viewType) {
                LibraryViewType.LIST -> lazyListState.animateScrollToItem(0)
                LibraryViewType.GRID -> lazyGridState.animateScrollToItem(0)
            }
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    var showCreatePlaylistDialog by rememberSaveable { mutableStateOf(false) }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            initialTextFieldValue = initialTextFieldValue,
            allowSyncing = allowSyncing,
            onPlaylistCreated = { playlistId ->
                showCreatePlaylistDialog = false
                navController.navigate("local_playlist/$playlistId")
            }
        )
    }

    // ── Shortcut editor bottom sheet UI ──────────────────────────────────
    if (showShortcutEditor) {
        ModalBottomSheet(
            onDismissRequest = { showShortcutEditor = false },
            sheetState = sheetState,
        ) {
            Text(
                text = stringResource(R.string.edit_library_shortcuts),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            data class ShortcutItem(val label: String, val shown: Boolean, val toggle: (Boolean) -> Unit)
            val shortcuts = listOf(
                ShortcutItem(stringResource(R.string.show_liked_playlist), showLiked, onShowLikedChange),
                ShortcutItem(stringResource(R.string.show_downloaded_playlist), showDownloaded, onShowDownloadedChange),
                ShortcutItem(stringResource(R.string.show_cached_playlist), showCached, onShowCachedChange),
                ShortcutItem(stringResource(R.string.show_top_playlist), showTop, onShowTopChange),
                ShortcutItem(stringResource(R.string.show_uploaded_playlist), showUploaded, onShowUploadedChange),
            )
            shortcuts.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { item.toggle(!item.shown) }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(checked = item.shown, onCheckedChange = item.toggle)
                }
            }
            Spacer(Modifier.padding(bottom = 16.dp))
        }
    }

    // ── Header row ───────────────────────────────────────────────────────
    val headerContent = @Composable {
        LibrarySearchHeader(
            isSearchActive = isSearchActive,
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onBack = {
                isSearchActive = false
                viewModel.updateSearchQuery("")
            },
            keyboardController = keyboardController,
            modifier = Modifier.padding(start = 16.dp),
        ) {
            SortHeader(
                sortType = sortType,
                sortDescending = sortDescending,
                onSortTypeChange = { newSort ->
                    onSortTypeChange(newSort)
                    // Exit reorder mode when switching away from CUSTOM
                    if (newSort != PlaylistSortType.CUSTOM) reorderMode = false
                },
                onSortDescendingChange = onSortDescendingChange,
                sortTypeText = { st ->
                    when (st) {
                        PlaylistSortType.CREATE_DATE -> R.string.sort_by_create_date
                        PlaylistSortType.NAME -> R.string.sort_by_name
                        PlaylistSortType.SONG_COUNT -> R.string.sort_by_song_count
                        PlaylistSortType.LAST_UPDATED -> R.string.sort_by_last_updated
                        PlaylistSortType.CUSTOM -> R.string.sort_by_custom
                    }
                },
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = pluralStringResource(
                    R.plurals.n_playlist,
                    visibleResults.count { !it.autoPlaylist },
                    visibleResults.count { !it.autoPlaylist },
                ),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
            )

            // Lock / Unlock icon — toggles drag-and-drop mode (switches to CUSTOM sort)
            IconButton(
                onClick = {
                    if (sortType != PlaylistSortType.CUSTOM) {
                        onSortTypeChange(PlaylistSortType.CUSTOM)
                    }
                    reorderMode = !reorderMode
                    if (reorderMode) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(
                        if (reorderMode) R.drawable.lock_open else R.drawable.lock
                    ),
                    contentDescription = stringResource(
                        if (reorderMode) R.string.reorder_playlists else R.string.lock_playlist_order
                    ),
                    tint = if (reorderMode) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface,
                )
            }

            // Edit (Pencil) icon — opens shortcut visibility editor
            IconButton(
                onClick = { showShortcutEditor = true },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.edit),
                    contentDescription = stringResource(R.string.edit_library_shortcuts),
                )
            }

            IconButton(
                onClick = { isSearchActive = true },
                modifier = Modifier.padding(start = 8.dp).size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = stringResource(R.string.search),
                )
            }

            IconButton(
                onClick = { onViewTypeChange(viewType.toggle()) },
                modifier = Modifier.padding(end = 8.dp).size(40.dp),
            ) {
                Icon(
                    painter = painterResource(
                        when (viewType) {
                            LibraryViewType.LIST -> R.drawable.list
                            LibraryViewType.GRID -> R.drawable.grid_view
                        }
                    ),
                    contentDescription = stringResource(
                        when (viewType) {
                            LibraryViewType.LIST -> R.string.switch_to_grid_view
                            LibraryViewType.GRID -> R.string.switch_to_list_view
                        }
                    ),
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (viewType) {
            LibraryViewType.LIST -> {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
                ) {
                    item(key = "filter", contentType = CONTENT_TYPE_HEADER) { filterContent() }
                    item(key = "header", contentType = CONTENT_TYPE_HEADER) { headerContent() }

                    if (visibleResults.isEmpty()) {
                        item(key = "empty_placeholder") {
                            if (searchQuery.isNotBlank()) {
                                LibrarySearchEmptyPlaceholder(modifier = Modifier.animateItem())
                            } else {
                                LibrarySearchEmptyPlaceholder(
                                    modifier = Modifier.animateItem(),
                                    icon = R.drawable.playlist_play,
                                    text = stringResource(R.string.library_playlist_empty),
                                )
                            }
                        }
                    }

                    // Auto-playlists (non-draggable)
                    items(
                        items = autoItems,
                        key = { it.key },
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) { item ->
                        PlaylistListItem(
                            playlist = item.playlist,
                            autoPlaylist = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { item.route?.let(navController::navigate) }
                                .animateItem(),
                        )
                    }

                    // User playlists — reorderable when reorderMode is on
                    items(
                        items = mutablePlaylists,
                        key = { it.key },
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) { item ->
                        if (reorderMode && sortType == PlaylistSortType.CUSTOM) {
                            ReorderableItem(state = reorderableState, key = item.key) {
                                LibraryPlaylistListItem(
                                    menuState = menuState,
                                    coroutineScope = coroutineScope,
                                    playlist = item.playlist,
                                    modifier = Modifier
                                        .animateItem()
                                        .longPressDraggableHandle(
                                            onDragStarted = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                        ),
                                )
                            }
                        } else {
                            LibraryPlaylistListItem(
                                menuState = menuState,
                                coroutineScope = coroutineScope,
                                playlist = item.playlist,
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }

            LibraryViewType.GRID -> {
                LazyVerticalGrid(
                    state = lazyGridState,
                    columns = GridCells.Adaptive(
                        minSize = GridThumbnailHeight + if (gridItemSize == GridItemSize.BIG) 24.dp else (-24).dp,
                    ),
                    contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
                ) {
                    item(key = "filter", span = { GridItemSpan(maxLineSpan) }, contentType = CONTENT_TYPE_HEADER) {
                        filterContent()
                    }
                    item(key = "header", span = { GridItemSpan(maxLineSpan) }, contentType = CONTENT_TYPE_HEADER) {
                        headerContent()
                    }

                    if (visibleResults.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            if (searchQuery.isNotBlank()) {
                                LibrarySearchEmptyPlaceholder(modifier = Modifier.animateItem())
                            } else {
                                LibrarySearchEmptyPlaceholder(
                                    modifier = Modifier.animateItem(),
                                    icon = R.drawable.playlist_play,
                                    text = stringResource(R.string.library_playlist_empty),
                                )
                            }
                        }
                    }

                    items(
                        items = autoItems,
                        key = { it.key },
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) { item ->
                        PlaylistGridItem(
                            playlist = item.playlist,
                            fillMaxWidth = true,
                            autoPlaylist = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(onClick = { item.route?.let(navController::navigate) })
                                .animateItem(),
                        )
                    }

                    items(
                        items = mutablePlaylists,
                        key = { it.key },
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) { item ->
                        LibraryPlaylistGridItem(
                            menuState = menuState,
                            coroutineScope = coroutineScope,
                            playlist = item.playlist,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreatePlaylistDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current
                        .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                )
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = stringResource(R.string.create_playlist),
            )
        }
    }
}
