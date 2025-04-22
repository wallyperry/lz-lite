package run.perry.lz.ui.intent

import run.perry.lz.base.IUiIntent

sealed class BootIntent : IUiIntent {
    data class GetConfig(val path: String) : BootIntent()
}