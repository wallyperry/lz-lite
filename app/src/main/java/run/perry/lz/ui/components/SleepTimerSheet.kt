package run.perry.lz.ui.components

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import run.perry.lz.R
import run.perry.lz.base.BaseBottomSheet
import run.perry.lz.databinding.SheetSleepTimerBinding
import run.perry.lz.domain.model.SleepTimerModel
import run.perry.lz.player.PlayerManager
import run.perry.lz.ui.adapter.SleepTimerAdapter
import run.perry.lz.utils.toFormattedDuration
import run.perry.lz.utils.toastSuccess
import run.perry.lz.utils.toastWarning

class SleepTimerSheet : BaseBottomSheet<SheetSleepTimerBinding>({ SheetSleepTimerBinding.inflate(it) }) {

    private val rvAdapter by lazy { SleepTimerAdapter() }

    override fun main() {

        if (PlayerManager.getController().isSleepTimer()) {
            binding.recyclerView.visibility = View.GONE
            binding.tvTimer.visibility = View.VISIBLE
            binding.btnPositive.text = "停止"
            PlayerManager.getController().sleepTimer.observe(this) {
                binding.tvTimer.text = it.toFormattedDuration(false, true)
                if (it <= 0) dismiss()
            }
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.tvTimer.visibility = View.GONE
            binding.btnPositive.text = "确定"
            initRecyclerView()
            val timers = listOf(
                10L to "10分钟",
                20L to "20分钟",
                30L to "30分钟",
                45L to "45分钟",
                60L to "1小时",
                90L to "1.5小时",
                120L to "2小时",
                150L to "2.5小时",
                180L to "3小时"
            )
            val timerList = timers.map { (value, text) -> SleepTimerModel(text, value) }
            rvAdapter.submitList(timerList)
        }

        binding.btnPositive.setOnClickListener {
            if (PlayerManager.getController().isSleepTimer()) {
                PlayerManager.getController().stopSleepTimer()
                "已停止睡眠定时器".toastSuccess()
            } else {
                val item = rvAdapter.getSelectedItem()
                if (item == null){
                    "请选择定时时间".toastWarning()
                    return@setOnClickListener
                }
                PlayerManager.getController().setSleepTimer(item.value ?: 0)
                "设置成功，将在${item.text}后停止播放".toastSuccess()
            }
            dismiss()
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.adapter = rvAdapter
        binding.recyclerView.setHasFixedSize(true)
        rvAdapter.addOnItemChildClickListener(R.id.tv) { adapter, view, position ->
            rvAdapter.selectedPosition(position)
        }
    }
}