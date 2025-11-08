package cz.visualio.archeologie.androidApp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import arrow.core.extensions.list.zip.zipWith
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import cz.visualio.archeologie.androidApp.databinding.FragmentCreditsBinding
import cz.visualio.archeologie.shared.model.Thematic

class CreditsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentCreditsBinding.inflate(inflater,container,false).also { view ->
            val thematic: Thematic = CreditsFragmentArgs.fromBundle(requireArguments()).thematic.thematic

            val activity = activity as AppCompatActivity

            activity.setSupportActionBar(view.toolbar2)
            val actionBar = activity.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)

            setHasOptionsMenu(true)

            val tvAuthor: TextView = view.author
            val tvAuthorTitle: TextView = view.authorTitle
            tvAuthor.text = thematic.author
            if(thematic.author == null){
                tvAuthor.isVisible = false
                tvAuthorTitle.isVisible = false
            }
            val tvInCoop: TextView = view.inCoop
            val tvInCoopTitle: TextView = view.inCoopTitle
            tvInCoop.text = thematic.professionalCooperation
            if(thematic.professionalCooperation == null){
                tvInCoop.isVisible = false
                tvInCoopTitle.isVisible = false
            }
            val tvInArtisticCoop: TextView = view.inArtisticCoop
            val tvInArtisticCoopTitle: TextView = view.inArtisticCoopTitle
            tvInArtisticCoop.text = thematic.artisticCooperation
            if(thematic.artisticCooperation == null){
                tvInArtisticCoop.isVisible = false
                tvInArtisticCoopTitle.isVisible = false
            }
            val tvAck: TextView = view.acknowledgements
            val tvAckTitle: TextView = view.acknowledgementsTitle
            tvAck.text = thematic.thanks
            if(thematic.thanks == null){
                tvAck.isVisible = false
                tvAckTitle.isVisible = false
            }

            listOf(
                view.logo1,
                view.logo2,
                view.logo3,
                view.logo4,
            ).zipWith(
                arg1 = thematic.logos.filter(String::isNotBlank),
                arg2 = { view, url ->
                    Glide.with(view)
                        .load(url)
                        .fitCenter()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(view)
                }
            )
        }.root

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        android.R.id.home -> {
            activity?.onBackPressed()
            true
        }
        else ->super.onOptionsItemSelected(item)
    }



}
