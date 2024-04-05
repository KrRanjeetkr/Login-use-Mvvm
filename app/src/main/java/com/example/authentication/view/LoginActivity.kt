package com.example.authentication.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.authentication.R
import com.example.authentication.data.LoginBody
import com.example.authentication.databinding.ActivityLoginBinding
import com.example.authentication.repository.AuthRepository
import com.example.authentication.utils.ApiService
import com.example.authentication.utils.VibrateView
import com.example.authentication.view_model.LoginActivityViewModel
import com.example.authentication.view_model.LoginActivityViewModelFactory
import java.lang.StringBuilder


class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener  {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mViewModel: LoginActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener(this)
        binding.loginWithGoggleBtn.setOnClickListener(this)
        binding.registerBtn.setOnClickListener(this)
        binding.emailEdt.onFocusChangeListener = this
        binding.passwordEdt.onFocusChangeListener = this
        binding.passwordEdt.setOnKeyListener(this)

        mViewModel = ViewModelProvider(this, LoginActivityViewModelFactory(AuthRepository(ApiService.getServices()), application)).get(LoginActivityViewModel::class.java)

        setupObservers()

    }

    private fun setupObservers() {
        mViewModel.getIsLoading().observe(this){
            binding.progressbar.isVisible = it
        }


        mViewModel.getErrorMessage().observe(this){
            //fullName, email, password
            val formErrorKeys = arrayOf("fullName", "email", "password")
            val message = StringBuilder()
            it.map { entry ->
                if (formErrorKeys.contains(entry.key)){
                    when(entry.key){
                        "email" -> {
                            binding.emailLayout.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }
                        }

                        "password" -> {
                            binding.passwordLayout.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }
                        }
//                        else -> {}
                    }
                }else{
                    message.append(entry.value).append("\n")
                }

                if (message.isNotEmpty()){
                    AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_info)
                        .setTitle("INFORMATION")
                        .setMessage(message)
                        .setPositiveButton("Ok"){dialog, _ -> dialog!!.dismiss()}
                        .show()
                }

            }
        }

        mViewModel.getUSer().observe(this){
            if (it != null){
                startActivity(Intent(this, HomeActivity::class.java))
            }
        }
    }

    private fun validateEmail(shouldUpdateView: Boolean = true, shouldVibrateView: Boolean = true) : Boolean {
        var errorMessage: String? = null
        val value: String = binding.emailEdt.text.toString()
        if (value.isEmpty()){
            errorMessage = "Email is required"
        }else if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()){
            errorMessage = "Email Address is Invalid"
        }

        if (errorMessage != null && shouldUpdateView){
            binding.emailLayout.apply {
                isErrorEnabled = true
                error = errorMessage
                if (shouldVibrateView) VibrateView.vibrate(this@LoginActivity, this)
            }
        }

        return errorMessage == null
    }

    private fun validatePassword(shouldUpdateView: Boolean = true, shouldVibrateView: Boolean = true) : Boolean {
        var errorMessage: String? = null
        val value: String = binding.passwordEdt.text.toString()
        if (value.isEmpty()){
            errorMessage = "Password is required"
        }else if (value.length < 6){
            errorMessage = "Password must be 6 character long"
        }

        if (errorMessage != null && shouldUpdateView){
            binding.passwordLayout.apply {
                isErrorEnabled = true
                error = errorMessage
                if (shouldVibrateView) VibrateView.vibrate(this@LoginActivity, this)
            }
        }

        return errorMessage == null
    }

    private fun validate() : Boolean{
        var isValid = true

        if (!validateEmail(shouldVibrateView = false)) isValid = false
        if (!validatePassword(shouldVibrateView = false)) isValid = false

        if (!isValid) VibrateView.vibrate(this, binding.cardView)

        return isValid
    }

    override fun onClick(view: View?) {
        if (view != null){
            when(view.id){
                R.id.loginBtn -> {
                    submitForm()
                }

                R.id.registerBtn -> {
                    startActivity(Intent(this, RegisterActivity::class.java))
                }
            }
        }
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null){
            when(view.id){


                R.id.emailEdt -> {
                    if (hasFocus){
                        if (binding.emailLayout.isErrorEnabled){
                            binding.emailLayout.isErrorEnabled = false
                        }
                    }else{
                        validateEmail()
                    }
                }

                R.id.passwordEdt -> {
                    if (hasFocus){
                        if (binding.passwordLayout.isErrorEnabled){
                            binding.passwordLayout.isErrorEnabled = false
                        }
                    }else{
                        validatePassword()
                    }
                }

            }
        }
    }

    private fun submitForm(){

        if (validate()){
            //verify user credential
            mViewModel.loginUser(LoginBody(binding.emailEdt.text!!.toString(), binding.passwordEdt.text!!.toString()))
        }

    }

    override fun onKey(view: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        if (keyCode== KeyEvent.KEYCODE_ENTER && keyEvent!!.action==KeyEvent.ACTION_UP){
            submitForm()
        }

        return false
    }

}