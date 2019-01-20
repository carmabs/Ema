package com.carmabs.ema.presentation.ui.defaultvm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.findNavController
import com.carmabs.ema.R
import com.carmabs.ema.presentation.ui.normalway.home.HomeFragment

class HomeActivity : AppCompatActivity(), NavHost {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.default_home_activity)
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment.newInstance())
                .commitNow()
        //navController.setGraph(R.navigation.navigation_home)
    }

    override fun getNavController(): NavController {

        try {
            return findNavController(R.id.navHostFragment)
        } catch (e: java.lang.RuntimeException) {
            throw RuntimeException("You must provide in your activity xml a fragment with " +
                    "android:id=@+ìd/navHostFragment as container " +
                    "with android:name=androidx.navigation.fragment.NavHostFragment")
        }

    }

    override fun onSupportNavigateUp() =
            findNavController(R.id.navHostFragment).navigateUp()
}
