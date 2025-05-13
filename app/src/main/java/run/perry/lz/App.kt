package run.perry.lz

import android.app.Application
import android.content.ComponentName
import androidx.appcompat.app.AppCompatDelegate
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.hjq.toast.Toaster
import com.umeng.commonsdk.UMConfigure
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import run.perry.lz.data.room.DatabaseManager
import run.perry.lz.di.appModule
import run.perry.lz.player.MusicService
import run.perry.lz.player.PlayerManager
import java.util.Locale


class App : Application() {

    override fun onCreate() {
        super.onCreate()

        initialization()
    }

    private fun initialization() {
        //默认语言
        Locale.setDefault(Locale.CHINA)

        //始终为日间模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        //Room
        DatabaseManager.init(this)

        //Koin
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        //Toast
        Toaster.init(this)

        //Player
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        val mediaControllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        mediaControllerFuture.addListener({
            val player = mediaControllerFuture.get()
            PlayerManager.setPlayer(player)
        }, MoreExecutors.directExecutor())

        //UpdateAppUtils
        //UpdateAppUtils.init(this)

        //Umeng
        UMConfigure.setLogEnabled(BuildConfig.DEBUG)
        UMConfigure.init(this, "68225af979267e0210611322", BuildConfig.FLAVOR, UMConfigure.DEVICE_TYPE_PHONE, "")
    }
}