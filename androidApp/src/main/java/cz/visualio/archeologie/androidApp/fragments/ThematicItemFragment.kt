package cz.visualio.archeologie.androidApp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import cz.visualio.archeologie.androidApp.databinding.ItemThematicBinding
import cz.visualio.archeologie.androidApp.parcelers.ThematicParcelable
import cz.visualio.archeologie.shared.model.Thematic


class ThematicItemFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ItemThematicBinding.inflate(inflater, container, false)
            .apply {
                val thematic = requireArguments().getParcelable<ThematicParcelable>(PARAM_THEMATIC)!!.thematic

                textView2.text = thematic.title

                Glide.with(this@ThematicItemFragment)
                    .load(thematic.imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .transform(CenterCrop(),RoundedCorners(40))
                    .into(imageView)
            }.root

    companion object {
        private const val PARAM_THEMATIC = "PARAM_THEMATIC"

        @JvmStatic
        fun newInstance(thematic: Thematic) =
            ThematicItemFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PARAM_THEMATIC, ThematicParcelable(thematic))
                }
            }
    }
}