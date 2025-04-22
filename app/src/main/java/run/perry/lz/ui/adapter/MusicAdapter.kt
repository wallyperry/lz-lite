package run.perry.lz.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import run.perry.lz.R
import run.perry.lz.data.room.entity.MusicEntity
import run.perry.lz.databinding.ItemMusicBinding
import run.perry.lz.utils.asColor

class MusicAdapter(private val showAlbum: Boolean = false) : BaseQuickAdapter<MusicEntity, MusicAdapter.VH>() {
    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: MusicEntity?
    ) {
        val index = holder.bindingAdapterPosition + 1

        holder.binding.apply {
            tvIndex.text = "$index"
            tvName.text = (item?.name.orEmpty()).trim().substringAfter("-")
            ivCached.visibility = if (item?.isCached == true) View.VISIBLE else View.GONE

            val artist = item?.artist.orEmpty().ifBlank { "李志" }
            val album = item?.album.orEmpty()
            tvArtist.text = listOfNotNull(artist, album.takeIf { showAlbum && it.isNotBlank() }).joinToString(" - ")

            item?.url?.let { url ->
                when {
                    listOf(".ape", ".wav", ".flac").any { url.contains(it, true) } -> {
                        ivSq.visibility = View.VISIBLE
                        ivSq.setImageResource(R.drawable.ic_sq)
                    }

                    listOf(".aac", ".m4a").any { url.contains(it, true) } -> {
                        ivSq.visibility = View.VISIBLE
                        ivSq.setImageResource(R.drawable.ic_hq)
                    }

                    else -> {
                        ivSq.visibility = View.GONE
                    }
                }
            }

            if (item?.playing == true) {
                tvName.setTextColor(R.color.purple_700.asColor)
                tvName.typeface = Typeface.DEFAULT_BOLD
                tvIndex.visibility = View.GONE
                ivPlaying.visibility = View.VISIBLE
            } else {
                tvName.setTextColor(R.color.black_3.asColor)
                tvName.typeface = Typeface.DEFAULT
                tvIndex.visibility = View.VISIBLE
                ivPlaying.visibility = View.GONE
            }
        }

    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ) = VH(parent)

    class VH(
        parent: ViewGroup,
        val binding: ItemMusicBinding = ItemMusicBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)
}