package run.perry.lz.player.datasource

import android.net.Uri

/**
 * Created by wangchenyan.top on 2024/3/26.
 */
object OnlineMusicUriFetcher {

    fun fetchPlayUrl(uri: Uri): String {
        //val songId = uri.getQueryParameter("id")?.toLongOrNull() ?: return uri.toString()
        //return runBlocking {
        //    val res = apiCall {
        //        DiscoverApi.get()
        //            .getSongUrl(songId, ConfigPreferences.playSoundQuality)
        //    }
        //
        //    if (res.isSuccessWithData() && res.getDataOrThrow().isNotEmpty()) {
        //        return@runBlocking res.getDataOrThrow().first().url
        //    } else {
        //        return@runBlocking ""
        //    }
        //}
        return ""
    }
}