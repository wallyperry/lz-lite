package run.perry.lz.ui.vm

import run.perry.lz.base.BaseViewModel
import run.perry.lz.base.IUiIntent
import run.perry.lz.data.room.dao.MusicDao
import run.perry.lz.domain.repository.ApiRepository
import run.perry.lz.ui.intent.MainIntent
import run.perry.lz.ui.state.MainState
import run.perry.lz.ui.state.MainUiState

class MainViewModel(private val api: ApiRepository, private val musicDao: MusicDao) :
    BaseViewModel<MainState, MainIntent>() {

    override suspend fun initUiState() = MainState(MainUiState.INIT)

    override suspend fun handleIntent(intent: IUiIntent) {
    }

}