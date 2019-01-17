package com.carmabs.ema.presentation.ui.defaultvm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.findNavController
import com.carmabs.ema.R

class DefaultVmActivity : AppCompatActivity(), NavHost {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.default_vm_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, DefaultVmFragment.newInstance())
                    .commitNow()
        }
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
