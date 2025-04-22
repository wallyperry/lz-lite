package run.perry.lz.ui.state

import run.perry.lz.base.IUiState

data class MusicSearchState(val musicSearchUiState: MusicSearchUiState) : IUiState

sealed class MusicSearchUiState {
    object INIT : MusicSearchUiState()
}