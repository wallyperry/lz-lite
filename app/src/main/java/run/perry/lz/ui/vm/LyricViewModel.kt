package run.perry.lz.ui.vm

import run.perry.lz.base.BaseViewModel
import run.perry.lz.base.IUiIntent
import run.perry.lz.domain.repository.RawRepository
import run.perry.lz.ui.intent.LyricIntent
import run.perry.lz.ui.state.LyricState
import run.perry.lz.ui.state.LyricUiState
import run.perry.lz.utils.LrcCache
import run.perry.lz.utils.getLrc

class LyricViewModel(private val raw: RawRepository) :
    BaseViewModel<LyricState, LyricIntent>() {
    override suspend fun initUiState() = LyricState(LyricUiState.INIT)

    override suspend fun handleIntent(intent: IUiIntent) {
        when (intent) {
            is LyricIntent.GetLyric -> {
                val lrcUrl = intent.item.mediaMetadata.getLrc().orEmpty()

                if (lrcUrl.isBlank()) {
                    sendUiState { copy(LyricUiState.ERROR(intent.item)) }
                    return
                }

                requestAnyWithFlow(
                    request = { raw.raw(lrcUrl) },
                    success = { response ->
                        val result = response?.takeIf { it.isSuccessful }?.body()?.string()
                        if (result.isNullOrEmpty()) {
                            sendUiState { copy(LyricUiState.ERROR(intent.item)) }
                        } else {
                            val file = LrcCache.saveLrcFile(intent.item, result)
                            sendUiState { copy(LyricUiState.SUCCESS(file.path)) }
                        }
                    },
                    error = { sendUiState { copy(LyricUiState.ERROR(intent.item)) } }
                )

            }
        }
    }

}