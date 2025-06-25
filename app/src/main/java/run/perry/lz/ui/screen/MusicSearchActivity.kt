package run.perry.lz.ui.screen

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.gyf.immersionbar.ktx.immersionBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import run.perry.lz.R
import run.perry.lz.base.BaseActivity
import run.perry.lz.databinding.ActivitySearchMusicBinding
import run.perry.lz.player.PlayerManager
import run.perry.lz.player.ProxyCacheManager
import run.perry.lz.ui.adapter.MusicAdapter
import run.perry.lz.ui.vm.MusicSearchViewModel
import run.perry.lz.utils.collectLatestOnLifecycle
import run.perry.lz.utils.inflateStateView
import run.perry.lz.utils.showDynamicPopup
import run.perry.lz.utils.toMediaItem
import run.perry.lz.utils.toastError
import run.perry.lz.utils.toastSuccess

class MusicSearchActivity : BaseActivity<ActivitySearchMusicBinding>({ ActivitySearchMusicBinding.inflate(it) }) {

    private val mKeyword by lazy { intent.getStringExtra("keyword").orEmpty() }

    private val mViewModel by viewModel<MusicSearchViewModel>()
    private val rvAdapter by lazy { MusicAdapter(showAlbum = true) }

    override fun main() {
        initBar()
        initRecyclerView()

        PlayerManager.getController().currentSong.observe(this) {
            if (it == null) return@observe
            CoroutineScope(Dispatchers.Main).launch {
                val list = rvAdapter.items.toMutableList()
                withContext(Dispatchers.IO) {
                    list.forEachIndexed { index, item ->
                        val isCached = ProxyCacheManager.isFullyCached(item.url)
                        list[index] = item.copy(isCached = isCached)
                    }
                }
                rvAdapter.submitList(list)
            }
        }

        mViewModel.searchMusic(mKeyword).collectLatestOnLifecycle(this) {
            delay(300)
            val list = it.toMutableList()
            withContext(Dispatchers.IO) {
                it.forEachIndexed { index, item ->
                    val isCached = ProxyCacheManager.isFullyCached(item.url)
                    list[index] = item.copy(isCached = isCached)
                }
            }
            rvAdapter.submitList(list)
            rvAdapter.stateView = inflateStateView(R.raw.lottie_empty, "暂无搜索结果")
            binding.tvTitle.text = "找到${list.size}条结果"
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
            PlayerManager.getController().addAndPlay(item.toMediaItem())
            startActivity(Intent(this, PlayerActivity::class.java))
        }

        rvAdapter.addOnItemChildClickListener(R.id.ib_more) { adapter, view, position ->
            val item = rvAdapter.getItem(position)
            if (item == null) {
                "添加失败".toastError()
                return@addOnItemChildClickListener
            }
            view.showDynamicPopup(
                listOf("添加到播放列表" to {
                    PlayerManager.getController().add(item.toMediaItem())
                    "已添加".toastSuccess()
                })
            )
        }
    }

    private fun initBar() {
        immersionBar {
            statusBarView(binding.statusBarView)
            statusBarDarkFont(true)
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
        }

        binding.apply {
            ibBack.setOnClickListener { backPressed() }
            tvTitle.text = "搜索结果"
        }
    }
}