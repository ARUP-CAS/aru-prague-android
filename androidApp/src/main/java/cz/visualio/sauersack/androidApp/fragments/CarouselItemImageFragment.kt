package cz.visualio.sauersack.androidApp.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import cz.visualio.sauersack.androidApp.databinding.FragmentCarouselItemImageBinding


class CarouselItemImageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        FragmentCarouselItemImageBinding.inflate(inflater, container, false).apply {
            val image = requireArguments().getParcelable<ContentType.Image>(ARG_PARAM)!!
            println("CarouselItemImageFragment")
            bottomSheet.isVisible = image.title.isNotBlank()
            if (image.title.isNotBlank()) {
                textView.setHtml(image.title)

                val bsb = BottomSheetBehavior.from(bottomSheet)
                bsb.state = BottomSheetBehavior.STATE_COLLAPSED
            }

/*            imageView2.load(image.url){
                crossfade(true)
                scale(Scale.FIT)
            }*/
            Glide.with(imageView2)
                .asBitmap()
                .load(image.url)
                .fitCenter()
                .transition(withCrossFade())
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?,
                    ) {
                        imageView2.setImageBitmap(resource)
                    }
                })


/*            Glide.with(imageView2)
                .load(image.url)
                .fitCenter()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView2)*/
        }.root

    companion object {
        private const val ARG_PARAM = "ARG_PARAM"

        @JvmStatic
        fun newInstance(content: ContentType.Image) =
            CarouselItemImageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM, content)
                }
            }
    }
}

