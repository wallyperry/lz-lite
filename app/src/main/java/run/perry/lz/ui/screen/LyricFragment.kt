package run.perry.lz.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import run.perry.lz.R
import run.perry.lz.base.BaseFragment
import run.perry.lz.databinding.FragmentLyricBinding
import run.perry.lz.player.PlayerManager
import run.perry.lz.ui.intent.LyricIntent
import run.perry.lz.ui.state.LyricUiState
import run.perry.lz.ui.vm.LyricViewModel
import run.perry.lz.utils.LrcCache
import run.perry.lz.utils.asString
import java.io.File

class LyricFragment(private val click: (() -> Unit)? = null) :
    BaseFragment<FragmentLyricBinding>({ FragmentLyricBinding.inflate(it) }) {

    private val mViewModel by viewModel<LyricViewModel>()

    override fun main() {

        binding.lrcView.run {
            setOnTapListener { v, x, y -> click?.invoke() }
            setDraggable(true) { view, time ->
                val playState = PlayerManager.getController().playState.value
                if (playState.isPlaying || playState.isPausing) {
                    PlayerManager.getController().seekTo(time.toInt())
                    if (playState.isPausing) {
                        PlayerManager.getController().playPause()
                    }
                    return@setDraggable true
                }
                return@setDraggable false
            }
        }

        PlayerManager.getController().currentSong.observe(this) {
            it?.run {
                binding.lrcView.loadLrc("")
                binding.lrcView.setLabel(R.string.lrc_loading.asString)
                mViewModel.sendUiIntent(LyricIntent.GetLyric(it))
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                PlayerManager.getController().playProgress.collectLatest {
                    binding.lrcView.run { if (hasLrc()) updateTime(it) }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                mViewModel.uiStateFlow.map { it.lyricUiState }.collectLatest {
                    when (it) {
                        is LyricUiState.INIT -> {}
                        is LyricUiState.SUCCESS -> binding.lrcView.loadLrc(File(it.filepath))
                        is LyricUiState.ERROR -> {
                            val lrcPath = LrcCache.getLrcFilePath(it.item)
                            if (lrcPath?.isNotEmpty() == true) {
                                binding.lrcView.loadLrc(File(lrcPath))
                                return@collectLatest
                            }
                            binding.lrcView.setLabel(R.string.lrc_no.asString)
                        }
                    }
                }
            }
        }

    }
}