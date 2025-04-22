package run.perry.lz.ui.state

import run.perry.lz.base.IUiState

data class AlbumState(val homeUiState: AlbumUiState) : IUiState

sealed class AlbumUiState {
    object INIT : AlbumUiState()
}