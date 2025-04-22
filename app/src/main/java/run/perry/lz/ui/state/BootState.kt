package run.perry.lz.ui.state

import run.perry.lz.base.IUiState
import run.perry.lz.domain.bean.Config

data class BootState(val configUiState: ConfigUiState) : IUiState

sealed class ConfigUiState {
    object INIT : ConfigUiState()
    data class SUCCESS(val bean: Config?) : ConfigUiState()
    object DONE : ConfigUiState()
}