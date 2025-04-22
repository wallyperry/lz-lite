package run.perry.lz.domain.bean

data class Music(
    val name: String?,          //专辑名
    val cover: String?,         //封面
    val info: String?,          //描述
    val year: String?,          //年份
    val state: Int?,            //状态，1=上架
    val list: List<MusicList?>?   //歌曲列表
) {
    data class MusicList(
        val name: String?,      //歌名
        val artist: String?,    //艺术家
        val url: String?,       //播放链接
        val lrc: String?,       //歌词
        val state: Int?         //状态，1=上架
    )
}