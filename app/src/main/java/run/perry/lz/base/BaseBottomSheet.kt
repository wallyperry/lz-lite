package run.perry.lz.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheet<VB : ViewBinding>(val block: (LayoutInflater) -> VB) : BottomSheetDialogFragment() {

    private var _binding: VB? = null
    protected val binding: VB get() = requireNotNull(_binding) { "The property of binding has been destroyed." }

    protected abstract fun main()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = block(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        main()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}