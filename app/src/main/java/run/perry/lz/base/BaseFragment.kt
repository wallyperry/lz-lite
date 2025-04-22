package run.perry.lz.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding>(val block: (LayoutInflater) -> VB) : Fragment() {

    private var _binding: VB? = null
    protected val binding: VB get() = requireNotNull(_binding) { "The property of binding has been destroyed." }

    private var isViewCreated = false  // 确保视图已经创建
    private var isVisibleToUser = false  // 标记Fragment是否对用户可见
    private var isDataLoaded = false  // 标记数据是否已经加载

    protected abstract fun main()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = block(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        main()
        isViewCreated = true
        loadDataIfVisible()  // 确保视图创建后加载数据
    }

    override fun onResume() {
        super.onResume()
        isVisibleToUser = true
        loadDataIfVisible()  // 在 Fragment 可见时加载数据
    }

    override fun onPause() {
        super.onPause()
        isVisibleToUser = false
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isVisibleToUser = !hidden
        loadDataIfVisible()  // 当可见性变化时重新加载数据
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 判断Fragment是否可见并且数据未加载时，加载数据
    private fun loadDataIfVisible() {
        if (isViewCreated && isVisibleToUser && !isDataLoaded) {
            lazyLoad()
            isDataLoaded = true
        }
    }

    open fun lazyLoad() {}

}