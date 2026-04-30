package cz.visualio.sauersack.androidApp.fragments

import androidx.fragment.app.Fragment
import cz.visualio.sauersack.androidApp.shared.analytics.AnalyticsManager

abstract class AnalyticsFragment(private val route: String) : Fragment() {

    private var screenStartTime: Long = 0L

    override fun onResume() {
        super.onResume()
        //   screenStartTime = System.currentTimeMillis()

        AnalyticsManager.logScreenNameEvent(route)
    }

    override fun onPause() {
        super.onPause()
//        val durationSeconds = (System.currentTimeMillis() - screenStartTime) / 1000
//
//        AnalyticsManager.logScreenDurationEvent(route, durationSeconds)
    }
}