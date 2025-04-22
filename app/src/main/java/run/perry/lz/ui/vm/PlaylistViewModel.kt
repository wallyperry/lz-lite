package run.perry.lz.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import run.perry.lz.base.BaseViewModel
import run.perry.lz.base.IUiIntent
import run.perry.lz.data.room.dao.PlaylistDao
import run.perry.lz.data.room.entity.SongEntity
import run.perry.lz.ui.intent.PlaylistIntent
import run.perry.lz.ui.state.PlaylistState
import run.perry.lz.ui.state.PlaylistUiState

class PlaylistViewModel(
    playlistDao: PlaylistDao
) : BaseViewModel<PlaylistState, PlaylistIntent>() {
    override suspend fun initUiState() = PlaylistState(PlaylistUiState.INIT)

    val playlist: StateFlow<List<SongEntity>> = playlistDao.getPlaylist()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override suspend fun handleIntent(intent: IUiIntent) {
    }
}