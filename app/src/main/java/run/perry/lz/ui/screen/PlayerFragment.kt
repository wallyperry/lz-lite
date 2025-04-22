package run.perry.lz.ui.screen

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.Coil
import coil.request.ImageRequest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import run.perry.lz.R
import run.perry.lz.base.BaseFragment
import run.perry.lz.databinding.FragmentPlayerBinding
import run.perry.lz.player.PlayerManager
import run.perry.lz.player.PlayState
import run.perry.lz.utils.toMusicEntity

class PlayerFragment(private val click: (() -> Unit)? = null) :
    BaseFragment<FragmentPlayerBinding>({ FragmentPlayerBinding.inflate(it) }) {

    override fun main() {
        binding.llRoot.setOnClickListener { click?.invoke() }
        binding.vinylAlbumView.initNeedle(PlayerManager.getController().playState.value.isPlaying)

        PlayerManager.getController().currentSong.observe(this) {
            val defaultCoverBitmap = BitmapFactory.decodeResource(resources, R.drawable.bg_playing_default_cover)
            binding.vinylAlbumView.setCoverBitmap(defaultCoverBitmap)
            if (it == null) {
                binding.vinylAlbumView.reset()
                return@observe
            }
            Coil.imageLoader(requireActivity()).enqueue(
                ImageRequest.Builder(requireActivity())
                    .data(it.toMusicEntity().cover)
                    .target { result ->
                        val bitmap = (result as? BitmapDrawable)?.bitmap ?: result.toBitmap()
                        binding.vinylAlbumView.setCoverBitmap(bitmap)
                    }
                    .build())
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                PlayerManager.getController().playState.collectLatest {
                    when (it) {
                        PlayState.Idle -> binding.vinylAlbumView.pause()
                        PlayState.Pause -> binding.vinylAlbumView.pause()
                        PlayState.Playing -> binding.vinylAlbumView.start()
                        PlayState.Preparing -> binding.vinylAlbumView.pause()
                    }
                }
            }
        }
    }
}