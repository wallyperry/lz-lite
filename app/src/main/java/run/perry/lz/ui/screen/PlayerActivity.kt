package run.perry.lz.ui.screen

import android.content.Intent
import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import run.perry.lz.R
import run.perry.lz.base.BaseActivity
import run.perry.lz.databinding.ActivityPlayerBinding
import run.perry.lz.player.PlayMode
import run.perry.lz.player.PlayState
import run.perry.lz.player.PlayerManager
import run.perry.lz.ui.components.SleepTimerSheet
import run.perry.lz.utils.FragmentSwitcher
import run.perry.lz.utils.asString
import run.perry.lz.utils.showDynamicPopup
import run.perry.lz.utils.toFormattedDuration
import run.perry.lz.utils.toMusicEntity
import run.perry.lz.utils.toastInfo

class PlayerActivity : BaseActivity<ActivityPlayerBinding>({ ActivityPlayerBinding.inflate(it) }) {

    private val mPlayerFragment: PlayerFragment by lazy {
        PlayerFragment { fragmentSwitcher.switchTo(mLyricFragment) }
    }
    private val mLyricFragment: LyricFragment by lazy {
        LyricFragment { fragmentSwitcher.switchTo(mPlayerFragment) }
    }

    private lateinit var fragmentSwitcher: FragmentSwitcher

    override fun main() {
        initBar()

        fragmentSwitcher = FragmentSwitcher(this, R.id.player_content)
        fragmentSwitcher.switchTo(mPlayerFragment)

        //循环模式
        binding.ibPlayMode.setOnClickListener {
            val mode = when (PlayerManager.getController().playMode.value) {
                PlayMode.Loop -> PlayMode.Shuffle
                PlayMode.Shuffle -> PlayMode.Single
                PlayMode.Single -> PlayMode.Loop
            }
            mode.nameRes.asString.toastInfo()
            PlayerManager.getController().setPlayMode(mode)
        }

        //上一曲
        binding.ibPrev.setOnClickListener {
            PlayerManager.getController().prev()
        }

        //播放&暂停
        binding.ibPlayPause.setOnClickListener {
            PlayerManager.getController().playPause()
        }

        //下一曲
        binding.ibNext.setOnClickListener {
            PlayerManager.getController().next()
        }

        //播放列表
        binding.ibPlaylist.setOnClickListener {
            startActivity(Intent(this, PlaylistActivity::class.java))
        }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) binding.tvTimeCurrent.text = progress.toLong().toFormattedDuration()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                PlayerManager.getController().seekTo(binding.seekbar.progress)
            }

        })

        PlayerManager.getController().currentSong.observe(this) {
            it?.toMusicEntity()?.also { entity ->
                binding.tvName.text = entity.name.orEmpty().ifBlank { R.string.app_name.asString }
                binding.tvArtist.text = "${entity.artist} - ${entity.album}"
                binding.tvArtist.visibility = View.VISIBLE
                binding.jukeboxLayout.setBackgroundCover(entity.cover.orEmpty(), 300)
            } ?: run {
                binding.tvName.text = R.string.app_name.asString
                binding.tvArtist.visibility = View.GONE
                binding.jukeboxLayout.setBackgroundResource(R.drawable.shape_jukebox_bg_default)
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                PlayerManager.getController().songDuration.collectLatest {
                    binding.tvTimeDuration.text = it.toFormattedDuration()
                    binding.seekbar.max = it.toInt()
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                PlayerManager.getController().playProgress.collectLatest {
                    binding.tvTimeCurrent.text = it.toFormattedDuration()
                    binding.seekbar.progress = it.toInt()
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                PlayerManager.getController().bufferedPosition.collectLatest {
                    binding.seekbar.secondaryProgress = it.toInt()
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                PlayerManager.getController().playState.collectLatest {
                    when (it) {
                        PlayState.Idle -> setPlayButton(1)
                        PlayState.Pause -> setPlayButton(1)
                        PlayState.Playing -> setPlayButton(2)
                        PlayState.Preparing -> setPlayButton(0)
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                PlayerManager.getController().playMode.collectLatest {
                    binding.ibPlayMode.setImageLevel(it.value)
                }
            }
        }
    }

    private fun setPlayButton(state: Int) {
        when (state) {
            0 -> {  //缓冲中
                binding.loading.visibility = View.VISIBLE
                binding.ibPlayPause.visibility = View.GONE
            }

            1 -> {  //暂停中
                binding.loading.visibility = View.GONE
                binding.ibPlayPause.visibility = View.VISIBLE
                binding.ibPlayPause.setImageResource(R.drawable.ic_bar_play)
            }

            2 -> {  //播放中
                binding.loading.visibility = View.GONE
                binding.ibPlayPause.visibility = View.VISIBLE
                binding.ibPlayPause.setImageResource(R.drawable.ic_bar_pause)
            }
        }
    }

    private fun initBar() {
        ImmersionBar.with(this)
            .statusBarView(binding.statusBarView)
            .transparentNavigationBar()
            .navigationBarAlpha(0f)
            .init()

        binding.navigationBarView.layoutParams = binding.navigationBarView.layoutParams.apply {
            height = ImmersionBar.getNavigationBarHeight(this@PlayerActivity)
        }

        binding.ibBack.setOnClickListener { finish() }
        binding.ibRight.setOnClickListener {
            it.showDynamicPopup(
                listOf(
                    "睡眠定时器" to {
                        SleepTimerSheet().show(supportFragmentManager, "timer_sleep")
                    }
                )
            )
        }
    }
}