package run.perry.lz.domain.http.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface RawService {

    @GET
    suspend fun raw(
        @Url
        url: String
    ): Response<ResponseBody>
}