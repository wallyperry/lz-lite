package run.perry.lz.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.Player

object PlayerManager {

    private var player: Player? = null
    private var controller: PlayerController? = null

    private val _isPlayerReady = MutableLiveData(false)
    val isPlayerReady: LiveData<Boolean> = _isPlayerReady

    fun setPlayer(player: Player) {
        this.player = player
        _isPlayerReady.value = true
    }

    fun getPlayer() = player ?: throw IllegalStateException("Player not prepared!")

    fun getController() = controller ?: run {
        val player = player ?: throw IllegalStateException("Player not prepared!")
        PlayerControllerImpl(player).also {
            controller = it
        }
    }

}