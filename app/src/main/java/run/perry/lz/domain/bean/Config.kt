package run.perry.lz.domain.bean

data class Config(
    val music: String?,
    val splash: Splash?,
    val banner: Banner?,
    val drawer: Drawer?,
    val version: Version?
) {
    data class Splash(
        val img: String?,
        val url: String?
    )

    data class Banner(
        val height: String?,
        val list: List<ListBean?>?
    ) {
        data class ListBean(
            val title: String?,
            val img: String?,
            val url: String?
        )
    }

    data class Drawer(
        val img: String?,
        val title: String?,
        val info: String?
    )

    data class Version(
        val name: String?,
        val title: String?,
        val info: String?,
        val url: String?,
        val force: Boolean?
    )
}