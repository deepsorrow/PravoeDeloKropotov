package com.krop.pravoedelokropotov.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.krop.pravoedelokropotov.databinding.FragmentAuthPhoneBinding
import com.krop.pravoedelokropotov.ui.MainActivity.Companion.OPEN_OTP_KEY
import com.krop.pravoedelokropotov.ui.OtpFragment.Companion.PHONE_NUMBER_KEY
import com.krop.pravoedelokropotov.ui.vm.PhoneAuthScreenVm
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class PhoneAuthFragment : Fragment() {

    private lateinit var viewModel: PhoneAuthScreenVm
    private var binding: FragmentAuthPhoneBinding? = null

    private var progressBarDialog: ProgressBarDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthPhoneBinding.inflate(layoutInflater)
        progressBarDialog = ProgressBarDialog(requireContext())

        binding!!.buttonNext.setOnClickListener {
            viewModel.sendCode()
        }
        bindKeypad()

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[PhoneAuthScreenVm::class.java]

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
            viewModel.phoneNumber.collect {
                binding?.phoneNumberInput?.setText(viewModel.formatNumber())
            }
        }

        lifecycleScope.launch {
            viewModel.goNextVisible.collect {
                binding?.buttonNext?.visibility = if (it) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.goToOtpScreen.collect { bundle ->
                goToOtpFragment(bundle)
            }
        }

        lifecycleScope.launch {
            viewModel.showError.collect {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun goToOtpFragment(params: Bundle) {
        val result = params.apply {
            putString(PHONE_NUMBER_KEY, viewModel.phoneNumber.value)
        }
        parentFragmentManager.setFragmentResult(OPEN_OTP_KEY, result)
    }

    private fun bindKeypad() {
        val keypadKeyListener: (v: View) -> Unit = { view: View ->
            val digit = (view as TextView).text.first()
            viewModel.inputDigit(digit)
        }

        binding?.keypad?.run {
            key0.setOnClickListener(keypadKeyListener)
            key1.setOnClickListener(keypadKeyListener)
            key2.setOnClickListener(keypadKeyListener)
            key3.setOnClickListener(keypadKeyListener)
            key4.setOnClickListener(keypadKeyListener)
            key5.setOnClickListener(keypadKeyListener)
            key6.setOnClickListener(keypadKeyListener)
            key7.setOnClickListener(keypadKeyListener)
            key8.setOnClickListener(keypadKeyListener)
            key9.setOnClickListener(keypadKeyListener)
            keyBackspace.setOnClickListener {
                viewModel.backspaceDigit()
            }
        }
    }

    companion object {
        val tag: String = PhoneAuthFragment::class.java.simpleName

        fun newInstance() = PhoneAuthFragment()
    }
}