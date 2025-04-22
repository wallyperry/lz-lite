package run.perry.lz.ui.state

import androidx.media3.common.MediaItem
import run.perry.lz.base.IUiState

data class LyricState(val lyricUiState: LyricUiState) : IUiState

sealed class LyricUiState {
    object INIT : LyricUiState()
    data class SUCCESS(val filepath: String) : LyricUiState()
    data class ERROR(val item: MediaItem) : LyricUiState()
}