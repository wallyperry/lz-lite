package run.perry.lz.ui.intent

import androidx.media3.common.MediaItem
import run.perry.lz.base.IUiIntent

sealed class LyricIntent : IUiIntent {
    data class GetLyric(val item: MediaItem) : LyricIntent()
}