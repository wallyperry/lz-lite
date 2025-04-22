package run.perry.lz.ui.components

import run.perry.lz.base.BaseBottomSheet
import run.perry.lz.databinding.SheetSleepTimerBinding
import run.perry.lz.utils.applyFullHeightDialog
import run.perry.lz.utils.disableShapeAnimation

class SleepTimerSheet : BaseBottomSheet<SheetSleepTimerBinding>({ SheetSleepTimerBinding.inflate(it) }) {

    override fun main() {

        dialog?.disableShapeAnimation()
        dialog?.applyFullHeightDialog(requireActivity())

        binding.btnNegative.setOnClickListener { dismiss() }
        binding.btnPositive.setOnClickListener { dismiss() }
    }
}