package run.perry.lz.ui.state

import run.perry.lz.base.IUiState

data class PlaylistState(val playlistUiState: PlaylistUiState) : IUiState

sealed class PlaylistUiState {
    object INIT : PlaylistUiState()
    object OnPlayMusicSingle : PlaylistUiState()
    object OnRemoveSingle : PlaylistUiState()
    object OnRemoveAll : PlaylistUiState()
}