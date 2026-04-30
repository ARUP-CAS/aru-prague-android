package cz.visualio.sauersack.androidApp.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import cz.visualio.sauersack.androidApp.R
import cz.visualio.sauersack.androidApp.databinding.FragmentCarouselItemSphereImageBinding
import java.io.ByteArrayOutputStream

class SphereImagesItemFragment : Fragment() {

    private var panoramaTarget: CustomTarget<Bitmap>? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        FragmentCarouselItemSphereImageBinding.inflate(inflater, container, false).apply {

            val ar = requireArguments().getParcelable<ContentType.SphereImages>(ARG_PARAM)!!

            bottomSheet.isVisible = ar.text.isNotBlank()

            panoWebView.apply {
                settings.javaScriptEnabled = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true

                loadUrl("file:///android_asset/pano_viewer.html")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        panoramaTarget = object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?,
                            ) {
                                val baos = ByteArrayOutputStream()
                                resource.compress(Bitmap.CompressFormat.JPEG, 90, baos)
                                val base64 =
                                    Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

                                val js = "loadPanorama('data:image/jpeg;base64,$base64')"
                                view?.evaluateJavascript(js, null)

                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        }

                        Glide.with(requireContext())
                            .asBitmap()
                            .load(ar.url)
                            .into(panoramaTarget!!)
                    }
                }

                setOnTouchListener { v, event ->
                    val vp2 = parentFragment?.view?.findViewById<ViewPager2>(R.id.carouselViewPager)
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> vp2?.isUserInputEnabled =
                            false

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            vp2?.isUserInputEnabled = true
                            v.performClick()
                        }

                    }
                    false
                }
            }
        }.root

    override fun onDestroyView() {
        super.onDestroyView()

        panoramaTarget?.let { Glide.with(this).clear(it) }
        panoramaTarget = null


        view?.findViewById<WebView>(R.id.panoWebView)?.apply {
            loadUrl("about:blank")
            stopLoading()
            clearHistory()
            clearCache(true)
            removeAllViews()
            destroy()
        }
    }

    companion object {
        private const val ARG_PARAM = "ARG_PARAM"

        @JvmStatic
        fun newInstance(content: ContentType.SphereImages) =
            SphereImagesItemFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM, content)
                }
            }
    }
}