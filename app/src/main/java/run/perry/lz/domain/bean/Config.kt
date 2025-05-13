package run.perry.lz.domain.bean

data class Config(
    val music: String?,
    val splash: Splash?,
    val banner: Banner?,
    val drawer: Drawer?,
    val version: Version?,
    val contact: Contact?,
    val share: Share?
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
        val code:Int?,
        val name: String?,
        val title: String?,
        val info: String?,
        val url: String?,
        val force: Boolean?
    )

    data class Contact(
        val title: String?,
        val content: String
    )

    data class Share(
        val qr: String?,
        val url: String?
    )
}