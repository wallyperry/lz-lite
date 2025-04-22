package run.perry.lz.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import run.perry.lz.base.BaseViewModel
import run.perry.lz.base.IUiIntent
import run.perry.lz.data.room.dao.AlbumDao
import run.perry.lz.data.room.dao.BannerDao
import run.perry.lz.data.room.dao.MusicDao
import run.perry.lz.data.room.entity.AlbumEntity
import run.perry.lz.data.room.entity.BannerEntity
import run.perry.lz.data.room.entity.MusicEntity
import run.perry.lz.domain.bean.Music
import run.perry.lz.domain.repository.ApiRepository
import run.perry.lz.ui.intent.AlbumIntent
import run.perry.lz.ui.state.AlbumState
import run.perry.lz.ui.state.AlbumUiState
import run.perry.lz.utils.Log

class AlbumViewModel(
    private val api: ApiRepository,
    private val albumDao: AlbumDao,
    private val musicDao: MusicDao,
    private val bannerDao: BannerDao
) : BaseViewModel<AlbumState, AlbumIntent>() {

    val allBanner: StateFlow<List<BannerEntity>> = bannerDao.getAllBanner()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allAlbumsSortedByYear: StateFlow<List<AlbumEntity>> = albumDao.getAllAlbumsSortedByYear()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override suspend fun initUiState() = AlbumState(AlbumUiState.INIT)

    override suspend fun handleIntent(intent: IUiIntent) {
        when (intent) {
            is AlbumIntent.GetMusic -> {
                requestAnyWithFlow(
                    request = { api.music(intent.baseurl, intent.path) },
                    success = { saveAlbum(it) }
                )
            }
        }
    }

    private suspend fun saveAlbum(list: List<Music?>?) {
        withContext(Dispatchers.IO) {
            val albumEntities = list?.mapIndexedNotNull { index, album ->
                album?.run {
                    AlbumEntity(
                        id = index + 1,
                        name = name?.trim()?.substringAfter("-"),
                        cover = cover,
                        info = info,
                        year = year,
                        state = state
                    )
                }
            } ?: emptyList()

            var musicId = 1
            val musicEntities = list?.flatMap { album ->
                album?.list?.mapNotNull { music ->
                    music?.run {
                        MusicEntity(
                            id = musicId++,
                            name = name?.trim()?.substringAfter("-"),
                            album = album.name?.trim()?.substringAfter("-"),
                            artist = artist,
                            cover = album.cover,
                            url = url,
                            lrc = lrc,
                            state = state
                        )
                    }
                } ?: emptyList()
            } ?: emptyList()

            albumDao.apply {
                if (albumEntities.isNotEmpty()) {
                    insertAlbum(albumEntities)
                    Log.i("${albumEntities.size} album data were inserted into the database")
                }
            }

            musicDao.apply {
                if (musicEntities.isNotEmpty()) {
                    insertMusic(musicEntities)
                    Log.i("${musicEntities.size} music data were inserted into the database")
                }
            }
        }
    }
}