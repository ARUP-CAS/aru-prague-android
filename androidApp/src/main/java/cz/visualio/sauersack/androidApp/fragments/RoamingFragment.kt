package cz.visualio.sauersack.androidApp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import cz.visualio.sauersack.androidApp.databinding.FragmentRoamingBinding
import cz.visualio.sauersack.androidApp.shared.analytics.Screen

class RoamingFragment : AnalyticsFragment(Screen.Roaming.route) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        FragmentRoamingBinding.inflate(inflater, container, false).apply {
            close.setOnClickListener {
                findNavController(this@RoamingFragment).navigate(
                    RoamingFragmentDirections.actionRoamingFragmentToSplashFragment()
                )
            }
        }.root
}