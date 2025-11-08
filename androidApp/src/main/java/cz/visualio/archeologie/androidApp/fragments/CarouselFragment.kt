package cz.visualio.archeologie.androidApp.fragments

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cz.visualio.archeologie.androidApp.adapters.ViewPager2Adapter
import cz.visualio.archeologie.androidApp.databinding.FragmentCarouselBinding
import cz.visualio.archeologie.shared.model.res.Content
import cz.visualio.archeologie.shared.model.res.LocationRes
import kotlinx.parcelize.Parcelize

sealed class ContentType {
    @Parcelize
    data class Video(
        val title: String,
        val urlVideo: String,
        val urlImage: String,
    ) : ContentType(),
        Parcelable

    @Parcelize
    data class File(
        val urlIos: String,
        val urlAndroid: String,
        val text: String,
        val sort: Long,
    ) : ContentType(), Parcelable

    @Parcelize
    data class Image(
        val title: String,
        val url: String,
    ) : ContentType(), Parcelable

    @Parcelize
    data class Text(val text: String) : ContentType(), Parcelable

    @Parcelize
    data class AR(
        val title: String,
        val urlFile: String,
        val urlImage: String,
    ) : ContentType(),
        Parcelable
}

class CarouselFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        FragmentCarouselBinding.inflate(inflater, container, false).apply {
            val location = CarouselFragmentArgs.fromBundle(requireArguments()).location.value

            val activity = activity as AppCompatActivity

            toolbar.title = location.title
            activity.setSupportActionBar(toolbar)
            val actionBar = activity.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)

            setHasOptionsMenu(true)

            val vpAdapter = ViewPager2Adapter<ContentType>(
                fragment = this@CarouselFragment,
                createFragment = { content ->
                    when (content) {
                        is ContentType.Video -> CarouselItemVideoFragment.newInstance(content)
                        is ContentType.Image -> CarouselItemImageFragment.newInstance(content)
                        is ContentType.Text -> CarouselItemTextFragment.newInstance(content)
                        is ContentType.AR -> CarouselItemARFragment.newInstance(content)
                        is ContentType.File -> CarouselItemFileFragment.newInstance(content)
                    }
                }
            )
            vpAdapter.update(location.toContentType())
            carouselViewPager.adapter = vpAdapter

            dotsIndicator.setViewPager2(carouselViewPager)

        }.root


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

}

fun LocationRes.toContentType(): List<ContentType> = content.flatMap {
    it.content.map {
        when (it) {
            is Content.Text -> ContentType.Text(it.text)
            is Content.Model -> ContentType.AR(
                title = it.text,
                urlImage = it.urlImage,
                urlFile = it.urlFile
            )
            is Content.Image -> ContentType.Image(title = it.text, url = it.url)
            is Content.Video -> ContentType.Video(
                title = it.text,
                urlVideo = it.urlVideo,
                urlImage = it.urlImage
            )
            is Content.File -> ContentType.File(
                text = it.text,
                urlAndroid = it.urlAndroid,
                sort = it.sort,
                urlIos = it.urlIos
            )
        }
    }
}
