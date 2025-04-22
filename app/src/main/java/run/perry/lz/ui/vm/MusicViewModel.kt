package run.perry.lz.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import run.perry.lz.base.BaseViewModel
import run.perry.lz.base.IUiIntent
import run.perry.lz.data.room.dao.MusicDao
import run.perry.lz.data.room.entity.MusicEntity
import run.perry.lz.ui.intent.MusicIntent
import run.perry.lz.ui.state.MusicState
import run.perry.lz.ui.state.MusicUiState

class MusicViewModel(
    private val musicDao: MusicDao
) : BaseViewModel<MusicState, MusicIntent>() {

    override suspend fun initUiState() = MusicState(MusicUiState.INIT)

    fun musicsByAlbum(album: String): StateFlow<List<MusicEntity>> = musicDao.getMusicsByAlbum(album)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override suspend fun handleIntent(intent: IUiIntent) {
    }
}