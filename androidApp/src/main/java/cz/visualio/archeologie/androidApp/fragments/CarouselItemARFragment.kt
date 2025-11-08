package cz.visualio.archeologie.androidApp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cz.visualio.archeologie.androidApp.databinding.FragmentCarouselItemARBinding

class CarouselItemARFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentCarouselItemARBinding.inflate(inflater, container, false).apply {
            val ar = requireArguments().getParcelable<ContentType.AR>(ARG_PARAM)!!
        }.root

    companion object {
        private const val ARG_PARAM = "ARG_PARAM"
        @JvmStatic
        fun newInstance(content: ContentType.AR) =
            CarouselItemARFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM, content)
                }
            }
    }
}