package run.perry.lz.base

import androidx.annotation.Keep

@Keep
interface IUiState

@Keep
interface IUiIntent

sealed class LoadUiIntent {
    data class Loading(val show: Boolean, val text: String? = null) : LoadUiIntent()

    data class Error(val show: Boolean, val msg: String? = null) : LoadUiIntent()
}