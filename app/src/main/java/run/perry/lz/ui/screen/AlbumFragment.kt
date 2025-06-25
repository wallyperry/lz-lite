package run.perry.lz.ui.screen

import android.content.Intent
import com.chad.library.adapter4.QuickAdapterHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import org.koin.androidx.viewmodel.ext.android.viewModel
import run.perry.lz.R
import run.perry.lz.base.BaseFragment
import run.perry.lz.data.AppStore
import run.perry.lz.databinding.FragmentAlbumBinding
import run.perry.lz.domain.http.utils.extractDomain
import run.perry.lz.domain.http.utils.extractPath
import run.perry.lz.ui.adapter.AlbumAdapter
import run.perry.lz.ui.adapter.AlbumHeaderAdapter
import run.perry.lz.ui.intent.AlbumIntent
import run.perry.lz.ui.state.AlbumUiState
import run.perry.lz.ui.vm.AlbumViewModel
import run.perry.lz.utils.Log
import run.perry.lz.utils.collectLatestOnLifecycle
import run.perry.lz.utils.inflateStateView

class AlbumFragment : BaseFragment<FragmentAlbumBinding>({ FragmentAlbumBinding.inflate(it) }) {

    private val mViewModel by viewModel<AlbumViewModel>()
    private val rvAdapter by lazy { AlbumAdapter() }
    private val headerAdapter by lazy { AlbumHeaderAdapter(requireActivity()) }
    private val adapterHelper by lazy { QuickAdapterHelper.Builder(rvAdapter).build() }

    override fun main() {
        initRecyclerView()

        mViewModel.allBanner.collectLatestOnLifecycle(this) {
            delay(300)
            if (it.isNotEmpty() && adapterHelper.beforeAdapterList.isEmpty()) {
                adapterHelper.addBeforeAdapter(headerAdapter)
                headerAdapter.item = it
            }
        }

        mViewModel.allAlbumsSortedByYear.collectLatestOnLifecycle(this) {
            delay(350)
            rvAdapter.submitList(it)
            rvAdapter.stateView = requireActivity().inflateStateView(R.raw.lottie_empty, "暂无数据")
            Log.d("load album list: submit size -> ${it.size}")
        }

        mViewModel.uiStateFlow.map { it.homeUiState }.collectLatestOnLifecycle(this) {
            when (it) {
                is AlbumUiState.INIT -> {}
            }
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.adapter = adapterHelper.adapter
        rvAdapter.isStateViewEnable = true
        rvAdapter.stateView = requireActivity().inflateStateView(R.raw.lottie_loading)
        rvAdapter.addOnItemChildClickListener(R.id.cv_root) { adapter, view, position ->
            val item = rvAdapter.getItem(position)
            startActivity(
                Intent(activity, MusicActivity::class.java).apply {
                    putExtra("name", item?.name.orEmpty())
                    putExtra("cover", item?.cover.orEmpty())
                    putExtra("info", item?.info.orEmpty())
                    putExtra("year", item?.year.orEmpty())
                }
            )
        }
    }

    override fun lazyLoad() {
        val domain = AppStore.musicApi.extractDomain()
        val path = AppStore.musicApi.extractPath()
        mViewModel.sendUiIntent(AlbumIntent.GetMusic(domain, path))
    }
}