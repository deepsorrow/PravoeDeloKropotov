package com.krop.pravoedelokropotov.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.krop.pravoedelokropotov.R
import com.krop.pravoedelokropotov.databinding.FragmentWelcomeBinding
import com.krop.pravoedelokropotov.ui.OtpFragment.Companion.PHONE_NUMBER_KEY

class WelcomeFragment : Fragment() {
    private var phoneNumber: String? = null
    private var token: String? = null

    private var binding: FragmentWelcomeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            phoneNumber = it.getString(PHONE_NUMBER_KEY)
            token = it.getString(TOKEN_KEY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(layoutInflater)

        val greetingTemplate = requireContext().getString(R.string.greeting_message)
        binding!!.greetingMessage.text = String.format(greetingTemplate, phoneNumber!!.drop(1), token)

        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {

        val tag: String = WelcomeFragment::class.java.simpleName

        const val TOKEN_KEY = "WelcomeFragment key"

        fun newInstance(phoneNumber: String, token: String) =
            WelcomeFragment().apply {
                arguments = Bundle().apply {
                    putString(PHONE_NUMBER_KEY, phoneNumber)
                    putString(TOKEN_KEY, token)
                }
            }
    }
}