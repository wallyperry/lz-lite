package run.perry.lz.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import run.perry.lz.base.BaseViewModel
import run.perry.lz.base.IUiIntent
import run.perry.lz.data.room.dao.MusicDao
import run.perry.lz.data.room.entity.MusicEntity
import run.perry.lz.ui.intent.MusicSearchIntent
import run.perry.lz.ui.state.MusicSearchState
import run.perry.lz.ui.state.MusicSearchUiState

class MusicSearchViewModel(private val musicDao: MusicDao) : BaseViewModel<MusicSearchState, MusicSearchIntent>() {
    override suspend fun initUiState() = MusicSearchState(MusicSearchUiState.INIT)

    fun searchMusic(keyword: String): StateFlow<List<MusicEntity>> = musicDao.searchMusic(keyword)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override suspend fun handleIntent(intent: IUiIntent) {
    }
}