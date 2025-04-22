package run.perry.lz.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.chad.library.adapter4.BaseQuickAdapter
import run.perry.lz.data.room.entity.AlbumEntity
import run.perry.lz.databinding.ItemAlbumBinding

class AlbumAdapter : BaseQuickAdapter<AlbumEntity, AlbumAdapter.VH>() {
    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: AlbumEntity?
    ) {
        holder.binding.tvName.text = (item?.name.orEmpty()).trim().substringAfter("-")
        holder.binding.ivCover.load(item?.cover)
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ) = VH(parent)

    class VH(
        parent: ViewGroup,
        val binding: ItemAlbumBinding = ItemAlbumBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

}