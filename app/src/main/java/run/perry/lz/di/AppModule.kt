package run.perry.lz.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import run.perry.lz.data.room.DatabaseManager
import run.perry.lz.domain.repository.ApiRepository
import run.perry.lz.domain.repository.RawRepository
import run.perry.lz.ui.vm.AlbumViewModel
import run.perry.lz.ui.vm.BootViewModel
import run.perry.lz.ui.vm.LyricViewModel
import run.perry.lz.ui.vm.MainViewModel
import run.perry.lz.ui.vm.MusicSearchViewModel
import run.perry.lz.ui.vm.MusicViewModel
import run.perry.lz.ui.vm.PlaylistViewModel

val repModule = module {
    single { ApiRepository() }
    single { RawRepository() }
}

val dbModule = module {

    //single {
    //    Room.databaseBuilder(
    //        get<Context>(),
    //        LzDatabase::class.java,
    //        "lz.db"
    //    ).build()
    //}

    single { DatabaseManager.getDatabase().albumDao() }
    single { DatabaseManager.getDatabase().musicDao() }
    single { DatabaseManager.getDatabase().bannerDao() }
    single { DatabaseManager.getDatabase().playlistDao() }
}

val vmModule = module {
    viewModel { BootViewModel(get(), get()) }
    viewModel { MainViewModel(get(), get()) }
    viewModel { AlbumViewModel(get(), get(), get(), get()) }
    viewModel { MusicViewModel(get()) }
    viewModel { LyricViewModel(get()) }
    viewModel { PlaylistViewModel(get()) }
    viewModel { MusicSearchViewModel(get()) }
}

//@SuppressLint("UnsafeOptInUsageError")
//val mediaModule = module {
//single { MediaCacheManager(get()) }
//single { get<MediaCacheManager>().simpleCache }
//single { get<MediaCacheManager>().cacheDataSourceFactory }
//}

val appModule = listOf(repModule, dbModule, vmModule)