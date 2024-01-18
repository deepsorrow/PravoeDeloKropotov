package com.krop.pravoedelokropotov.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.addCallback
import com.krop.pravoedelokropotov.databinding.ActivityMainBinding
import com.krop.pravoedelokropotov.ui.OtpFragment.Companion.PHONE_NUMBER_KEY
import com.krop.pravoedelokropotov.ui.WelcomeFragment.Companion.TOKEN_KEY

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        initFragmentResultListeners()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(binding!!.container.id, PhoneAuthFragment.newInstance(), PhoneAuthFragment.tag)
                .addToBackStack(null)
                .commit()
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (supportFragmentManager.fragments[0]::class.java != PhoneAuthFragment::class.java) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun initFragmentResultListeners() {
        supportFragmentManager.setFragmentResultListener(OPEN_OTP_KEY, this) { _, bundle ->
            val otpFragment = OtpFragment.newInstance(bundle)
            supportFragmentManager.beginTransaction()
                .replace(binding!!.container.id, otpFragment, OtpFragment.tag)
                .addToBackStack(null)
                .commit()
        }

        supportFragmentManager.setFragmentResultListener(OPEN_WELCOME_SCREEN_KEY, this) { _, bundle ->
            val welcomeFragment = WelcomeFragment.newInstance(
                bundle.getString(PHONE_NUMBER_KEY, ""),
                bundle.getString(TOKEN_KEY, "")
            )
            supportFragmentManager.beginTransaction()
                .replace(binding!!.container.id, welcomeFragment, WelcomeFragment.tag)
                .addToBackStack(null)
                .commit()
        }
    }

    companion object {
        const val OPEN_OTP_KEY = "Open otp key"
        const val OPEN_WELCOME_SCREEN_KEY = "Open welcome screen key"
    }
}