package run.perry.lz.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import run.perry.lz.R
import run.perry.lz.databinding.ItemSleepTimerBinding
import run.perry.lz.domain.model.SleepTimerModel
import run.perry.lz.utils.asColor

class SleepTimerAdapter : BaseQuickAdapter<SleepTimerModel, SleepTimerAdapter.VH>() {

    private var mSelectedPosition = -1

    override fun onBindViewHolder(
        holder: VH, position: Int, item: SleepTimerModel?
    ) {
        holder.binding.tv.text = item?.text.orEmpty()
        holder.binding.tv.setTextColor(
            if (mSelectedPosition == position) {
                R.color.black.asColor
            } else {
                R.color.black_6.asColor
            }
        )
        holder.binding.tv.run {
            text = item?.text.orEmpty()
            if (mSelectedPosition == position) {
                setTextColor(R.color.purple_700.asColor)
            } else {
                setTextColor(R.color.black_5.asColor)
            }
        }
    }

    fun selectedPosition(i: Int) {
        notifyItemChanged(mSelectedPosition)
        mSelectedPosition = i
        notifyItemChanged(mSelectedPosition)
    }

    fun getSelectedItem() = if (mSelectedPosition >= 0) {
        getItem(mSelectedPosition)
    } else null

    override fun onCreateViewHolder(
        context: Context, parent: ViewGroup, viewType: Int
    ) = VH(parent)

    class VH(
        parent: ViewGroup, val binding: ItemSleepTimerBinding = ItemSleepTimerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)
}