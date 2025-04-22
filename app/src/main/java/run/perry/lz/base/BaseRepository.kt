package run.perry.lz.base

open class BaseRepository {

    suspend fun <T : Any> executeAnyRequest(block: suspend () -> T?): T? {
        val data = block.invoke()
        return data
    }
}