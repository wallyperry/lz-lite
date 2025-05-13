package run.perry.lz.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.chad.library.adapter4.BaseSingleItemAdapter
import com.chad.library.adapter4.fullspan.FullSpanAdapterType
import com.umeng.analytics.MobclickAgent
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.config.BannerConfig
import com.youth.banner.config.IndicatorConfig
import com.youth.banner.indicator.CircleIndicator
import run.perry.lz.data.AppStore
import run.perry.lz.data.room.entity.BannerEntity
import run.perry.lz.databinding.ItemHeaderAlbumBannerBinding
import run.perry.lz.databinding.ItemHeaderAlbumBinding
import run.perry.lz.utils.UM_EVENT_BANNER
import run.perry.lz.utils.dp
import run.perry.lz.utils.openInBrowser

class AlbumHeaderAdapter(private val activity: FragmentActivity) :
    BaseSingleItemAdapter<List<BannerEntity>, AlbumHeaderAdapter.VH>(), FullSpanAdapterType {

    override fun onBindViewHolder(holder: VH, item: List<BannerEntity>?) {
        holder.binding.riv.heightToWidth(AppStore.bannerHeight.toFloatOrNull() ?: 0.3125f)
        holder.binding.banner.run {
            addBannerLifecycleObserver(activity)
            setAdapter(HeaderBannerAdapter(item ?: emptyList()))
            indicator = CircleIndicator(activity)
            setIndicatorGravity(IndicatorConfig.Direction.RIGHT)
            setIndicatorWidth(BannerConfig.INDICATOR_NORMAL_WIDTH, BannerConfig.INDICATOR_NORMAL_WIDTH)
            setIndicatorMargins(IndicatorConfig.Margins(0, 0, BannerConfig.INDICATOR_MARGIN, 10.dp))
            setOnBannerListener { data, position ->
                activity.openInBrowser((data as BannerEntity).url)
                MobclickAgent.onEventObject(activity, UM_EVENT_BANNER, mapOf("name" to data.title))
            }
        }
    }

    override fun onCreateViewHolder(
        context: Context, parent: ViewGroup, viewType: Int
    ) = VH(parent)

    class VH(
        parent: ViewGroup, val binding: ItemHeaderAlbumBinding = ItemHeaderAlbumBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)
}

class HeaderBannerAdapter(list: List<BannerEntity>) : BannerAdapter<BannerEntity, HeaderBannerAdapter.VH>(list) {
    override fun onCreateHolder(parent: ViewGroup, viewType: Int) = VH(parent)

    override fun onBindView(holder: VH, data: BannerEntity?, position: Int, size: Int) {
        holder.binding.iv.load(data?.img)
        holder.binding.tv.text = data?.title.orEmpty()
        holder.binding.tv.visibility = if (data?.title.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    class VH(
        parent: ViewGroup, val binding: ItemHeaderAlbumBannerBinding = ItemHeaderAlbumBannerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)
}