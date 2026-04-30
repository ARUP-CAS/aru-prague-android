package cz.visualio.sauersack.androidApp.shared.analytics

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import kotlinx.serialization.Serializable


@Serializable
sealed class Screen(val route: String) {

    object Credits : Screen("CreditsScreen")
    object Roaming : Screen("RoamingScreen")
    object Location : Screen("LocationScreen")
    object Gallery : Screen("GalleryScreen")
    object Thematic : Screen("ThematicScreen")

//    sealed class GalleryScreen(route: String) : Screen(route) {
//        object Text : GalleryScreen("TextGalleryScreen")
//        object Image : GalleryScreen("ImageGalleryScreen")
//        object Video : GalleryScreen("VideoGalleryScreen")
//        object AR : GalleryScreen("ARGalleryScreen")
//        object Image360 : GalleryScreen("Image360GalleryScreen")
//    }
}

interface Analytics {
    fun logScreenNameEvent(screenName: String)
    fun logScreenDurationEvent(screenName: String, logTime: Long)
}

private object RealAnalyticsManager : Analytics {

    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }

    override fun logScreenNameEvent(screenName: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        })
    }

    override fun logScreenDurationEvent(screenName: String, logTime: Long) {
//        firebaseAnalytics.logEvent(SCREEN_DURATION_SEC, Bundle().apply {
//            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
//            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
//            putLong(SCREEN_DURATION_SEC, logTime)
//        }
//        )

    }
}

private object FakeAnalyticsManager : Analytics {
    override fun logScreenNameEvent(screenName: String) {}
    override fun logScreenDurationEvent(screenName: String, logTime: Long) {}
}


object AnalyticsManager : Analytics by FakeAnalyticsManager // or fake
