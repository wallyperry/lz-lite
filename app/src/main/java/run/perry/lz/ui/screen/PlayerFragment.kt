package run.perry.lz.ui.screen

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import run.perry.lz.R
import run.perry.lz.base.BaseFragment
import run.perry.lz.databinding.FragmentPlayerBinding
import run.perry.lz.player.PlayState
import run.perry.lz.player.PlayerManager
import run.perry.lz.utils.collectLatestOnLifecycle
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

        PlayerManager.getController().playState.collectLatestOnLifecycle(this) {
            when (it) {
                PlayState.Idle -> binding.vinylAlbumView.pause()
                PlayState.Pause -> binding.vinylAlbumView.pause()
                PlayState.Playing -> binding.vinylAlbumView.start()
                PlayState.Preparing -> binding.vinylAlbumView.pause()
            }
        }
    }
}