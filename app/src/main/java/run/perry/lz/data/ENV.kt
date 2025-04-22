package run.perry.lz.data

import run.perry.lz.utils.appContext

object ENV {

    object APP {
        val SPLASH_CACHE_PATH = appContext().cacheDir.absolutePath + "/splash.jpg"
    }

}