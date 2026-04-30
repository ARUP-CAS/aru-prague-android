package cz.visualio.sauersack.androidApp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import cz.visualio.sauersack.androidApp.databinding.FragmentSplashBinding


class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        FragmentSplashBinding.inflate(inflater, container, false).apply {
            btnStart.setOnClickListener {
                findNavController(this@SplashFragment).navigate(
                    SplashFragmentDirections.actionSplashFragmentToMapFragment())
            }
        }.root
}