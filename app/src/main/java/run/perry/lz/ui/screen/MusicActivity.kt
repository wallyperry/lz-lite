package run.perry.lz.ui.screen

import android.content.Intent
import android.view.Gravity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.gyf.immersionbar.ktx.immersionBar
import com.gyf.immersionbar.ktx.statusBarHeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import run.perry.lz.R
import run.perry.lz.base.BaseActivity
import run.perry.lz.databinding.ActivityMusicBinding
import run.perry.lz.player.PlayerManager
import run.perry.lz.player.ProxyCacheManager
import run.perry.lz.ui.adapter.MusicAdapter
import run.perry.lz.ui.components.TipsDialog
import run.perry.lz.ui.vm.MusicViewModel
import run.perry.lz.utils.Log
import run.perry.lz.utils.asDimenPx
import run.perry.lz.utils.asString
import run.perry.lz.utils.collectLatestOnLifecycle
import run.perry.lz.utils.dp
import run.perry.lz.utils.inflateStateView
import run.perry.lz.utils.setMargin
import run.perry.lz.utils.setParams
import run.perry.lz.utils.showDynamicPopup
import run.perry.lz.utils.toMediaItem
import run.perry.lz.utils.toMediaItems
import run.perry.lz.utils.toastError
import run.perry.lz.utils.toastSuccess
import kotlin.math.abs

class MusicActivity : BaseActivity<ActivityMusicBinding>({ ActivityMusicBinding.inflate(it) }) {

    private val mName by lazy { intent.getStringExtra("name").orEmpty() }
    private val mCover by lazy { intent.getStringExtra("cover").orEmpty() }
    private val mInfo by lazy { intent.getStringExtra("info").orEmpty() }
    private val mYear by lazy { intent.getStringExtra("year").orEmpty() }

    private val mViewModel by viewModel<MusicViewModel>()
    private val rvAdapter by lazy { MusicAdapter() }

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

        mViewModel.musicsByAlbum(mName).collectLatestOnLifecycle(this) {
            delay(300)
            val list = it.toMutableList()
            withContext(Dispatchers.IO) {
                it.forEachIndexed { index, item ->
                    val isCached = ProxyCacheManager.isFullyCached(item.url)
                    list[index] = item.copy(isCached = isCached)
                }
            }
            rvAdapter.submitList(list)
            rvAdapter.stateView = inflateStateView(R.raw.lottie_empty, "暂无数据")
            Log.d("load music list(musics by album): submit size -> ${list.size}")
            binding.viewPlayAll.tvPlayAllCount.text =
                list.takeIf { it -> it.isNotEmpty() }?.let { it -> "（${it.size}）" } ?: ""
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = rvAdapter
        rvAdapter.isStateViewEnable = true
        rvAdapter.stateView = inflateStateView(R.raw.lottie_loading)
        rvAdapter.addOnItemChildClickListener(R.id.ll_root) { adapter, view, position ->
            val items = rvAdapter.items
            val item = rvAdapter.getItem(position)
            if (item == null) {
                "播放失败".toastError()
                return@addOnItemChildClickListener
            }
            PlayerManager.getController().replaceAll(items.toMediaItems(), item.toMediaItem())
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
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
        }

        binding.run {
            tvTitle.text = title
            toolbar.setParams(h = statusBarHeight + R.dimen.dp50.asDimenPx)
            toolbar1.setParams(h = statusBarHeight + R.dimen.dp50.asDimenPx)

            rlTitle.setMargin(0, statusBarHeight, 0, 0)

            appBarLayout.addOnOffsetChangedListener { l, i ->
                val alpha = abs(255f / l.totalScrollRange * i).toInt()
                toolbar1.background.alpha = alpha
                if (alpha > 170) {
                    tvTitle.text = mName.trim().substringAfter("-")
                } else {
                    tvTitle.text = R.string.menu_album.asString
                }
            }

            jukeboxLayout.apply {
                //唱片机背景高度=cover高+上下margin+toolbar高
                jukebox.setParams(h = (100 + 16 + 32).dp + toolbar.layoutParams.height)
                cvCover.setMargin(16.dp, 16.dp + toolbar.layoutParams.height, 16.dp, 32.dp)
                tvName.text = mName.trim().substringAfter("-")
                tvYear.text = if (mYear.isEmpty() || mYear == "9999") "未知年份" else mYear
                tvInfo.text = mInfo.ifBlank { "暂无详情" }
                ivCover.load(mCover)
                jukebox.setBackgroundCover(mCover, 300)

                tvInfo.setOnClickListener {
                    TipsDialog(this@MusicActivity).show {
                        title(mName.trim().substringAfter("-"))
                        content(mInfo.ifBlank { "暂无详情" }, Gravity.START)
                        hidePositive()
                        negative("好的")
                    }
                }
            }

            ibBack.setOnClickListener { backPressed() }
            ibMore.setOnClickListener {
                it.showDynamicPopup(
                    listOf(
                        "全部添加到播放列表" to {
                            rvAdapter.items.takeUnless { it.isEmpty() }?.also {
                                PlayerManager.getController().addAll(it.toMediaItems())
                                "已添加".toastSuccess()
                            } ?: "添加了个寂寞".toastError()
                        }
                    )
                )
            }

            viewPlayAll.llPlayAll.setOnClickListener {
                val items = rvAdapter.items
                PlayerManager.getController().replaceAll(items.toMediaItems(), items[0].toMediaItem())
                startActivity(Intent(this@MusicActivity, PlayerActivity::class.java))
            }
        }
    }
}