package cz.visualio.archeologie.androidApp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cz.visualio.archeologie.androidApp.databinding.FragmentCarouselItemFileBinding

class CarouselItemFileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentCarouselItemFileBinding.inflate(inflater, container, false).apply {
            val file = requireArguments().getParcelable<ContentType.File>(ARG_PARAM)!!
        }.root

    companion object {
        private const val ARG_PARAM = "ARG_PARAM"
        @JvmStatic
        fun newInstance(content: ContentType.File) =
            CarouselItemFileFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM, content)
                }
            }
    }
}