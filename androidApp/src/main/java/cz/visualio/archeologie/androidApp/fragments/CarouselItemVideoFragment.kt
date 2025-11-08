package cz.visualio.archeologie.androidApp.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import cz.visualio.archeologie.androidApp.databinding.FragmentCarouselItemVideoBinding

class CarouselItemVideoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentCarouselItemVideoBinding.inflate(inflater, container, false).apply {
            val video = requireArguments().getParcelable<ContentType.Video>(ARG_PARAM)!!
            bottomSheet.isVisible = video.title.isNotBlank()
            if(video.title.isNotBlank()) {
                textView.setHtml(video.title)

                val bsb = BottomSheetBehavior.from(bottomSheet)
                bsb.state = BottomSheetBehavior.STATE_EXPANDED
            }
            Glide.with(imageView2)
                .load(video.urlImage)
                .fitCenter()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView2)

            imageView2.setOnClickListener {
                Uri.parse(video.urlVideo)
                    .let{ Intent(Intent.ACTION_VIEW, it) }
                    .let(::startActivity)
            }
        }.root

    companion object {
        private const val ARG_PARAM = "ARG_PARAM"
        @JvmStatic
        fun newInstance(content: ContentType.Video) =
            CarouselItemVideoFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM, content)
                }
            }
    }
}