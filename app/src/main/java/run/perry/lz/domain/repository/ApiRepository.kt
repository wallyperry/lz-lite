package run.perry.lz.domain.repository

import run.perry.lz.base.BaseRepository
import run.perry.lz.domain.http.RetrofitHelper

class ApiRepository : BaseRepository() {

    private val service = RetrofitHelper.apiService

    suspend fun config(path: String) =
        executeAnyRequest { service.config(path) }

    suspend fun music(baseurl: String, path: String) =
        executeAnyRequest { service.music(baseurl, path) }
}