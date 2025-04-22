package run.perry.lz.player

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import run.perry.lz.BuildConfig
import run.perry.lz.R
import run.perry.lz.ui.screen.MainActivity
import run.perry.lz.utils.appContext

class MusicService : MediaSessionService() {

    companion object {
        val EXTRA_NOTIFICATION = "${appContext().packageName}.notification"
    }

    private lateinit var player: Player
    private lateinit var session: MediaSession

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val dataSourceFactory = DefaultDataSource.Factory(
            appContext(),
            DefaultHttpDataSource.Factory()
                .setUserAgent("LzLite/${BuildConfig.VERSION_NAME}")
                .setAllowCrossProtocolRedirects(true)
        )

        val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)

        player = ExoPlayer.Builder(appContext())
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        session = MediaSession.Builder(this, player)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java).apply {
                        putExtra(EXTRA_NOTIFICATION, true)
                        action = Intent.ACTION_VIEW
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(appContext()).build().apply {
                setSmallIcon(R.drawable.ic_notifcation_lizi)
            }
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return session
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        session.release()
    }

}