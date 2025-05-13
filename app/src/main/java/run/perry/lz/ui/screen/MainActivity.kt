package run.perry.lz.ui.screen

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.google.gson.Gson
import com.gyf.immersionbar.ktx.immersionBar
import com.gyf.immersionbar.ktx.statusBarHeight
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.launch
import run.perry.lz.R
import run.perry.lz.base.BaseActivity
import run.perry.lz.data.AppStore
import run.perry.lz.databinding.ActivityMainBinding
import run.perry.lz.databinding.NavHeaderMainBinding
import run.perry.lz.player.PlayState
import run.perry.lz.player.PlayerManager
import run.perry.lz.ui.components.SleepTimerSheet
import run.perry.lz.utils.ActivityManager
import run.perry.lz.utils.FragmentSwitcher
import run.perry.lz.utils.Log
import run.perry.lz.utils.asString
import run.perry.lz.utils.checkVersionUpdate
import run.perry.lz.utils.toMusicEntity
import run.perry.lz.utils.toastInfo
import run.perry.lz.utils.toastWarning

class MainActivity : BaseActivity<ActivityMainBinding>({ ActivityMainBinding.inflate(it) }) {

    private val backPressFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private lateinit var fragmentSwitcher: FragmentSwitcher

    private var searchView: SearchView? = null

    override fun main() {
        fragmentSwitcher = FragmentSwitcher(this, R.id.main_content)

        initBar()

        checkVersionUpdate()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                backPressFlow.buffer().map { System.currentTimeMillis() }
                    .runningFold(listOf<Long>()) { acc, value ->
                        (acc + value).takeLast(2)
                    }.filter { it.size == 2 && it[1] - it[0] < 2000 }
                    .collectLatest { ActivityManager.exit(this@MainActivity) }
            }
        }

        onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                        return
                    }
                    lifecycleScope.launch {
                        backPressFlow.emit(Unit)
                        "再按一次退出".toastInfo()
                        delay(2000)
                    }
                }
            })

        PlayerManager.getController().currentSong.observe(this) {
            Log.d("current song: ${Gson().toJson(it?.toMusicEntity())}")
            it?.toMusicEntity()?.also { entity ->
                binding.rlPlayBar.ivCover.load(entity.cover)
                binding.rlPlayBar.tvName.text = "${entity.name} - ${entity.artist}"
            } ?: run {
                binding.rlPlayBar.ivCover.load(R.drawable.ic_album)
                binding.rlPlayBar.tvName.text = R.string.app_name.asString
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                PlayerManager.getController().songDuration.collectLatest {
                    Log.d("song duration: $it")
                    binding.rlPlayBar.progressBar.max = it.toInt()
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                PlayerManager.getController().playProgress.collectLatest {
                    binding.rlPlayBar.progressBar.apply {
                        if (max > 0) progress = it.toInt()
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                PlayerManager.getController().playState.collectLatest {
                    binding.rlPlayBar.ivCover.syncWithState(it)
                    when (it) {
                        PlayState.Idle -> setPlayButton(1)
                        PlayState.Pause -> setPlayButton(1)
                        PlayState.Playing -> setPlayButton(2)
                        PlayState.Preparing -> setPlayButton(0)
                    }
                }
            }
        }
    }

    private fun setPlayButton(state: Int) {
        binding.rlPlayBar.run {
            when (state) {
                0 -> {  //缓冲中
                    loading.visibility = View.VISIBLE
                    ibPlayPause.visibility = View.GONE
                }

                1 -> {  //暂停中
                    loading.visibility = View.GONE
                    ibPlayPause.visibility = View.VISIBLE
                    ibPlayPause.setImageResource(R.drawable.ic_bar_play)
                }

                2 -> {  //播放中
                    loading.visibility = View.GONE
                    ibPlayPause.visibility = View.VISIBLE
                    ibPlayPause.setImageResource(R.drawable.ic_bar_pause)
                }
            }
        }
    }

    private fun initBar() {
        immersionBar {
            supportActionBar(true)
            statusBarDarkFont(true)
            navigationBarColor(R.color.white)
            navigationBarDarkIcon(true)
            statusBarView(binding.statusBarView)
        }

        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.drawer_open, R.string.drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_album -> {
                    fragmentSwitcher.switchTo(AlbumFragment::class)
                    updateSearchHint(it)
                    title = it.title
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }

                //R.id.nav_video -> {
                //    fragmentSwitcher.switchTo(VideoFragment::class)
                //    updateSearchHint(it)
                //    title = it.title
                //    binding.drawerLayout.closeDrawer(GravityCompat.START)
                //}

                R.id.nav_timer -> SleepTimerSheet().show(supportFragmentManager, SleepTimerSheet.TAG)
                //R.id.nav_setting -> startActivity(Intent(this, SettingActivity::class.java))
                R.id.nav_about -> startActivity(Intent(this, AboutActivity::class.java))
            }
            true
        }

        //初始选中
        binding.navView.menu.findItem(R.id.nav_album).apply {
            this@MainActivity.title = title
            isChecked = true
        }
        fragmentSwitcher.switchTo(AlbumFragment::class)

        NavHeaderMainBinding.bind(binding.navView.getHeaderView(0)).apply {
            statusBarView.layoutParams = statusBarView.layoutParams.apply {
                height = statusBarHeight
            }

            ivAvatar.load(AppStore.drawerImg) {
                error(R.mipmap.ic_launcher)
                placeholder(R.mipmap.ic_launcher)
            }

            tvTitle.text = AppStore.drawerTitle.ifBlank { R.string.app_name.asString }
            tvInfo.text = AppStore.drawerInfo
        }

        binding.rlPlayBar.apply {
            ibPlayPause.setOnClickListener { PlayerManager.getController().playPause() }
            ibPlaylist.setOnClickListener { startActivity(Intent(this@MainActivity, PlaylistActivity::class.java)) }
            rlRoot.setOnClickListener { startActivity(Intent(this@MainActivity, PlayerActivity::class.java)) }
        }
    }

    private fun updateSearchHint(item: MenuItem) {
        searchView?.queryHint = when (item.itemId) {
            R.id.nav_album -> "搜索歌曲"
            //R.id.nav_video -> "搜索视频"
            else -> "请输入关键词"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_main, menu)
        searchView = (menu?.findItem(R.id.action_search)?.actionView as SearchView?)?.apply {
            queryHint = "请输入关键词"
            findViewById<View>(androidx.appcompat.R.id.search_plate).background = null
            findViewById<View>(androidx.appcompat.R.id.submit_area).background = null
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    setQuery("", false)
                    onActionViewCollapsed()
                    if (query.isNullOrBlank()) {
                        "请输入关键词".toastWarning()
                        return false
                    }
                    when (binding.navView.checkedItem?.itemId) {
                        R.id.nav_album -> {
                            startActivity(
                                Intent(this@MainActivity, MusicSearchActivity::class.java).apply {
                                    putExtra("keyword", query)
                                }
                            )
                        }
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean = false

            })
        }
        return true
    }

    override fun onDestroy() {
        binding.rlPlayBar.ivCover.release()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        binding.rlPlayBar.ivCover.syncWithState(PlayerManager.getController().playState.value)
    }
}