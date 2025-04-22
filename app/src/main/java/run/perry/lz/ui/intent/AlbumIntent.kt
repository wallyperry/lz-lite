package run.perry.lz.ui.intent

import run.perry.lz.base.IUiIntent

sealed class AlbumIntent : IUiIntent {
    data class GetMusic(val baseurl: String, val path: String) : AlbumIntent()
}