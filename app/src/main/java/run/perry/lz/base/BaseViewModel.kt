package run.perry.lz.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import run.perry.lz.domain.http.utils.parseThrowable

abstract class BaseViewModel<UiState : IUiState, UiIntent : IUiIntent> : ViewModel() {

    protected abstract suspend fun initUiState(): UiState

    private val _uiStateFlow = MutableSharedFlow<UiState>(replay = 1)
    val uiStateFlow: SharedFlow<UiState> = _uiStateFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            _uiStateFlow.emit(initUiState()) // 确保初始化时 UI 订阅者能收到初始状态
        }
    }

    protected fun sendUiState(copy: UiState.() -> UiState) {
        viewModelScope.launch {
            val newState = copy(_uiStateFlow.replayCache.firstOrNull() ?: initUiState())
            _uiStateFlow.emit(newState) // 这里确保每次调用都会发送
        }
    }

    private val _uiIntentFlow: Channel<UiIntent> = Channel()
    val uiIntentFlow: Flow<UiIntent> = _uiIntentFlow.receiveAsFlow()

    private val _loadUiIntentFlow: Channel<LoadUiIntent> = Channel()
    val loadUiIntentFlow: Flow<LoadUiIntent> = _loadUiIntentFlow.receiveAsFlow()

    protected abstract suspend fun handleIntent(intent: IUiIntent)

    init {
        viewModelScope.launch {
            uiIntentFlow.collect {
                handleIntent(it)
            }
        }
    }

    fun sendUiIntent(uiIntent: UiIntent) {
        viewModelScope.launch {
            _uiIntentFlow.send(uiIntent)
        }
    }

    private fun sendLoadUiIntent(loadUiIntent: LoadUiIntent) {
        viewModelScope.launch {
            _loadUiIntentFlow.send(loadUiIntent)
        }
    }

    /**
     * 网络请求封装方法
     */
    protected fun <T : Any> requestAnyWithFlow(
        showLoading: Boolean = false,
        loadingText: String? = null,
        showError: Boolean = false,
        request: suspend () -> T?,
        start: suspend () -> Unit = {},
        success: suspend (T?) -> Unit = {},
        error: suspend (String) -> Unit = {
            //默认异常处理，子类可以进行覆写
            sendLoadUiIntent(LoadUiIntent.Error(showError, it))
        },
        done: suspend () -> Unit = {}
    ) {
        viewModelScope.launch {
            start.invoke()
            if (showLoading) sendLoadUiIntent(LoadUiIntent.Loading(true, loadingText))
            try {
                success(request())
            } catch (e: Exception) {
                error(parseThrowable(e))
            } finally {
                if (showLoading) sendLoadUiIntent(LoadUiIntent.Loading(false))
                done()
            }
        }
    }
}
