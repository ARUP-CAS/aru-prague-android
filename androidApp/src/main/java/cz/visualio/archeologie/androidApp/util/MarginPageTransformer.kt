package cz.visualio.archeologie.androidApp.util

import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class MarginPageTransformer(@Px private val mMarginPx: Int) : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        val viewPager = requireViewPager(page)
        val offset = mMarginPx * position
        if (viewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) page.translationX = offset
        else page.translationY = offset
    }

    private fun requireViewPager(page: View): ViewPager2 {
        val parent = page.parent as RecyclerView
        return parent.parent as ViewPager2
    }
}