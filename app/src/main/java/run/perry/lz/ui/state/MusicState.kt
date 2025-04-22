package run.perry.lz.ui.state

import run.perry.lz.base.IUiState

data class MusicState(val musicUiState: MusicUiState) : IUiState

sealed class MusicUiState {
    object INIT : MusicUiState()
}