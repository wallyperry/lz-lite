package run.perry.lz.ui.state

import run.perry.lz.base.IUiState
import run.perry.lz.data.room.entity.MusicEntity

data class MainState(val mainUiState: MainUiState) : IUiState

sealed class MainUiState {
    object INIT : MainUiState()
}