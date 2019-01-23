package com.carmabs.ema.presentation.ui.normalway.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carmabs.ema.R

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.default_home_activity)
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment.newInstance())
                .commitNow()

    }
}
