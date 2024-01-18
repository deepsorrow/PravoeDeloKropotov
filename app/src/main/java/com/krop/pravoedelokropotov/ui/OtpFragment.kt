package com.krop.pravoedelokropotov.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.krop.pravoedelokropotov.R
import com.krop.pravoedelokropotov.databinding.FragmentOtpBinding
import com.krop.pravoedelokropotov.model.api.GetCodeResponse
import com.krop.pravoedelokropotov.ui.MainActivity.Companion.OPEN_WELCOME_SCREEN_KEY
import com.krop.pravoedelokropotov.ui.WelcomeFragment.Companion.TOKEN_KEY
import com.krop.pravoedelokropotov.ui.vm.OtpScreenVm
import kotlinx.coroutines.launch

class OtpFragment : Fragment() {

    private lateinit var viewModel: OtpScreenVm
    private var binding: FragmentOtpBinding? = null

    private var progressBarDialog: ProgressBarDialog? = null

    private val phoneNumber by lazy { arguments?.getString(PHONE_NUMBER_KEY).orEmpty() }
    private val status by lazy { arguments?.getString(GetCodeResponse.STATUS_KEY).orEmpty() }
    private val code by lazy { arguments?.getString(GetCodeResponse.CODE_KEY).orEmpty() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtpBinding.inflate(layoutInflater)
        progressBarDialog = ProgressBarDialog(requireContext())

        binding!!.phoneNumber.text = phoneNumber
        binding!!.arrowGoBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        if (status == "old") {
            binding!!.run {
                codeSentText1.text = requireContext().resources.getText(R.string.code_was_already_sent1)
                codeSentText2.text = requireContext().resources.getText(R.string.code_was_already_sent2)
                codeSentText2.visibility = View.VISIBLE
                resendButton.visibility = View.VISIBLE
            }
        }

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[OtpScreenVm::class.java].apply {
            this.phoneNumber = this@OtpFragment.phoneNumber
            actualCode = code
        }

        lifecycleScope.launch {
            viewModel.progressBarVisibility.collect { isVisible ->
                if (isVisible) {
                    progressBarDialog?.show()
                } else {
                    progressBarDialog?.dismiss()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.showError.collect {
                binding?.codeEditText?.hideKeyboard()
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
            }
        }

        lifecycleScope.launch {
            viewModel.showResendButton.collect {
                binding?.resendButton?.visibility = View.VISIBLE
                binding?.countdownTimer?.visibility = View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.goToWelcomeScreenWithToken.collect { token ->
                parentFragmentManager.setFragmentResult(OPEN_WELCOME_SCREEN_KEY, Bundle().apply {
                    putString(PHONE_NUMBER_KEY, phoneNumber)
                    putString(TOKEN_KEY, token)
                })
            }
        }

        lifecycleScope.launch {
            viewModel.resendTimer.collect { secondsLeft ->
                secondsLeft == 0 && return@collect

                val timeLeft = String.format(requireContext().getString(R.string.countdown_timer_template), secondsLeft)

                binding?.resendButton?.visibility = View.GONE
                binding?.countdownTimer?.visibility = View.VISIBLE
                binding?.countdownTimer?.text = timeLeft
            }
        }

        binding?.resendButton?.setOnClickListener {
            viewModel.resendCode()
        }

        binding?.buttonNext?.setOnClickListener {
            viewModel.validateCode(binding!!.codeEditText.text.toString())
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    companion object {
        const val PHONE_NUMBER_KEY = "Phone number"
        val tag: String = OtpFragment::class.java.simpleName

        fun newInstance(arguments: Bundle) = OtpFragment().apply {
            this.arguments = arguments
        }
    }
}