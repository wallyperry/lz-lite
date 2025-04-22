package run.perry.lz.domain.repository

import run.perry.lz.base.BaseRepository
import run.perry.lz.domain.http.RetrofitHelper

class RawRepository : BaseRepository() {

    private val service = RetrofitHelper.rawService

    suspend fun raw(url: String) =
        executeAnyRequest { service.raw(url) }
}