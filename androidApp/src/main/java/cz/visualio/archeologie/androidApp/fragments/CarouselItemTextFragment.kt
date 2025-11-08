package cz.visualio.archeologie.androidApp.fragments

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import cz.visualio.archeologie.androidApp.databinding.FragmentCarouselItemTextBinding

class CarouselItemTextFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentCarouselItemTextBinding.inflate(inflater, container, false).apply {
            val text = requireArguments().getParcelable<ContentType.Text>(ARG_PARAM)!!
            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.setHtml(text.text)
        }.root

    companion object {
        private const val ARG_PARAM = "ARG_PARAM"
        @JvmStatic
        fun newInstance(content: ContentType.Text) =
            CarouselItemTextFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM, content)
                }
            }
    }
}

fun TextView.setHtml(html: String) {
    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    else
        Html.fromHtml(html)
}