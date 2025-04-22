package run.perry.lz.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import run.perry.lz.R
import run.perry.lz.base.BaseActivity
import run.perry.lz.databinding.ActivityPlaylistBinding
import run.perry.lz.player.PlayerManager
import run.perry.lz.player.ProxyCacheManager
import run.perry.lz.ui.adapter.MusicAdapter
import run.perry.lz.ui.vm.PlaylistViewModel
import run.perry.lz.utils.Log
import run.perry.lz.utils.inflateStateView
import run.perry.lz.utils.showDynamicPopup
import run.perry.lz.utils.toMediaItem
import run.perry.lz.utils.toMusicEntities
import run.perry.lz.utils.toastError
import run.perry.lz.utils.toastSuccess

class PlaylistActivity : BaseActivity<ActivityPlaylistBinding>({ ActivityPlaylistBinding.inflate(it) }) {

    private val rvAdapter by lazy { MusicAdapter(true) }
    private val mViewModel by viewModel<PlaylistViewModel>()
    private var mCurrentPlayingIndex = 0

    override fun main() {

        initBar()
        initRecyclerView()

        binding.fab.setOnClickListener {
            binding.recyclerView.smoothScrollToPosition(mCurrentPlayingIndex)
        }

        PlayerManager.getController().currentSong.observe(this) {
            if (it == null) return@observe
            CoroutineScope(Dispatchers.Main).launch {
                val list = rvAdapter.items.toMutableList()
                withContext(Dispatchers.IO) {
                    list.forEachIndexed { index, item ->
                        val isCached = ProxyCacheManager.isFullyCached(item.url)
                        list[index] = item.copy(isCached = isCached)
                        if (item.playing) mCurrentPlayingIndex = index
                    }
                }
                rvAdapter.submitList(list)
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                mViewModel.playlist.collectLatest {
                    delay(300)
                    val musicEntities = it.toMusicEntities()
                    val list = musicEntities.toMutableList()
                    withContext(Dispatchers.IO) {
                        musicEntities.forEachIndexed { index, item ->
                            val isCached = ProxyCacheManager.isFullyCached(item.url)
                            list[index] = item.copy(isCached = isCached)
                            if (item.playing) mCurrentPlayingIndex = index
                        }
                    }
                    rvAdapter.submitList(list)
                    rvAdapter.stateView = inflateStateView(R.raw.lottie_empty, "暂无数据")
                    binding.tvTitle.text = "$title(${list.size})"
                    Log.d("load playlist all(musics by playlist): submit size -> ${list.size}")
                }
            }
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = rvAdapter
        rvAdapter.isStateViewEnable = true
        rvAdapter.stateView = inflateStateView(R.raw.lottie_loading)
        rvAdapter.addOnItemChildClickListener(R.id.ll_root) { adapter, view, position ->
            val item = rvAdapter.getItem(position)
            if (item == null) {
                "播放失败".toastError()
                return@addOnItemChildClickListener
            }
            PlayerManager.getController().play("${item.id}")
        }

        rvAdapter.addOnItemChildClickListener(R.id.ib_more) { adapter, view, position ->
            val item = rvAdapter.getItem(position)
            if (item == null) {
                "移除失败".toastError()
                return@addOnItemChildClickListener
            }
            view.showDynamicPopup(
                listOf("从列表移除" to {
                    PlayerManager.getController().delete(item.toMediaItem())
                    "已移除".toastSuccess()
                })
            )
        }
    }

    private fun initBar() {
        ImmersionBar.with(this)
            .statusBarView(binding.statusBarView)
            .statusBarDarkFont(true)
            .navigationBarColor(R.color.white)
            .navigationBarDarkIcon(true)
            .init()

        binding.apply {
            ibBack.setOnClickListener { finish() }
            ibMore.setOnClickListener {
                it.showDynamicPopup(
                    listOf(
                        "清空播放列表" to {
                            PlayerManager.getController().clearPlaylist()
                            "已清空".toastSuccess()
                        }
                    )
                )
            }
            tvTitle.text = title
        }
    }
}