package run.perry.lz.ui.vm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import run.perry.lz.base.BaseViewModel
import run.perry.lz.base.IUiIntent
import run.perry.lz.data.AppStore
import run.perry.lz.data.room.dao.BannerDao
import run.perry.lz.data.room.entity.BannerEntity
import run.perry.lz.domain.bean.Config
import run.perry.lz.domain.repository.ApiRepository
import run.perry.lz.ui.intent.BootIntent
import run.perry.lz.ui.state.BootState
import run.perry.lz.ui.state.ConfigUiState
import run.perry.lz.utils.Log

class BootViewModel(private val api: ApiRepository, private val bannerDao: BannerDao) :
    BaseViewModel<BootState, BootIntent>() {

    override suspend fun initUiState() = BootState(ConfigUiState.INIT)

    override suspend fun handleIntent(intent: IUiIntent) {
        when (intent) {
            is BootIntent.GetConfig -> {
                requestAnyWithFlow(
                    showLoading = true,
                    loadingText = "初始化中...",
                    request = { api.config(intent.path) },
                    success = {
                        if (!it?.music.isNullOrBlank()) {
                            AppStore.musicApi = it.music
                        }

                        saveBanner(it?.banner?.list)
                        AppStore.run {
                            bannerHeight = it?.banner?.height.orEmpty()
                            drawerImg = it?.drawer?.img.orEmpty()
                            drawerTitle = it?.drawer?.title.orEmpty()
                            drawerInfo = it?.drawer?.info.orEmpty()

                            versionName = it?.version?.name.orEmpty()
                            versionTitle = it?.version?.title.orEmpty()
                            versionInfo = it?.version?.info.orEmpty()
                            versionUrl = it?.version?.url.orEmpty()
                            versionForce = it?.version?.force == true

                            shareQr = it?.share?.qr.orEmpty()
                            shareUrl = it?.share?.url.orEmpty()
                        }

                        sendUiState { copy(ConfigUiState.SUCCESS(it)) }
                    },
                    done = { sendUiState { copy(ConfigUiState.DONE) } })
            }
        }
    }

    private suspend fun saveBanner(list: List<Config.Banner.ListBean?>?) {
        withContext(Dispatchers.IO) {
            val entities = ArrayList<BannerEntity>()
            list?.forEachIndexed { i, it ->
                it?.run { entities.add(BannerEntity(id = i + 1, title = title, img = img, url = url)) }
            }
            bannerDao.clearBanner()
            if (entities.isNotEmpty()) bannerDao.insertBanner(entities)
            Log.i("${entities.size} banner data were inserted into the database")
        }
    }
}