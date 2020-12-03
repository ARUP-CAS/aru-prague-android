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
import cz.visualio.archeologie.androidApp.parcelers.LocationParcelable
import cz.visualio.archeologie.shared.model.res.LocationRes


class LocationItemFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ItemThematicBinding.inflate(inflater, container, false)
            .apply {
                val location = requireArguments().getParcelable<LocationParcelable>(PARAM_LOCATION)!!.value

                textView2.text = location.title

                Glide.with(this@LocationItemFragment)
                    .load(location.image)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .transform(CenterCrop() ,RoundedCorners(40))
                    .into(imageView)
            }.root

    companion object {
        private const val PARAM_LOCATION = "PARAM_LOCATION"

        @JvmStatic
        fun newInstance(location: LocationRes) =
            LocationItemFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PARAM_LOCATION, LocationParcelable(location))
                }
            }
    }
}