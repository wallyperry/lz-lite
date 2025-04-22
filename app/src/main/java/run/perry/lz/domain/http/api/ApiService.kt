package run.perry.lz.domain.http.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import run.perry.lz.domain.bean.Config
import run.perry.lz.domain.bean.Music

interface ApiService {

    @GET("{path}")
    suspend fun config(
        @Path(value = "path", encoded = true)
        path: String
    ): Config?

    @GET("{path}")
    suspend fun music(
        @Header("baseurl")
        baseurl: String,
        @Path(value = "path", encoded = true)
        path: String
    ): List<Music?>?

}