package com.example.authentication

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.authentication.databinding.ActivityRegisterBinding
import com.example.authentication.repository.AuthRepository
import com.example.authentication.utils.ApiService
import com.example.authentication.view_model.RegisterActivityViewModel
import com.example.authentication.view_model.RegisterActivityViewModelFactory
import java.lang.StringBuilder

class RegisterActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mViewModel: RegisterActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.fullNameEdt.onFocusChangeListener = this
        binding.emailEdt.onFocusChangeListener = this
        binding.passwordEdt.onFocusChangeListener = this
        binding.conformPasswordEdt.onFocusChangeListener = this
        mViewModel = ViewModelProvider(this, RegisterActivityViewModelFactory(AuthRepository(ApiService.getServices
            ()), application)).get(RegisterActivityViewModel::class.java)

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
                        "fullName" -> {
                            binding.fullNameLayout.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }
                        }

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

                        else -> {}
                    }
                }else{
                    message.append(entry.value).append("\n")
                }
            }
        }

        mViewModel.getUSer().observe(this){

        }
    }

    private fun validateFullName() : Boolean {
        var errorMessage: String? = null
        val value: String = binding.fullNameEdt.text.toString()
        if (value.isEmpty()){
            errorMessage = "Full name is required"
        }

        if (errorMessage != null){
            binding.fullNameLayout.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }
        return errorMessage == null
    }

    private fun validateEmail() : Boolean {
        var errorMessage: String? = null
        val value: String = binding.emailEdt.text.toString()
        if (value.isEmpty()){
            errorMessage = "Email is required"
        }else if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()){
            errorMessage = "Email Address is Invalid"
        }

        if (errorMessage != null){
            binding.emailLayout.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validatePassword() : Boolean {
        var errorMessage: String? = null
        val value: String = binding.passwordEdt.text.toString()
        if (value.isEmpty()){
            errorMessage = "Password is required"
        }else if (value.length < 6){
            errorMessage = "Password must be 6 character long"
        }

        if (errorMessage != null){
            binding.passwordLayout.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validateConformPassword() : Boolean {
        var errorMessage: String? = null
        val value: String = binding.conformPasswordEdt.text.toString()
        if (value.isEmpty()){
            errorMessage = "Conform Password is required"
        }else if (value.length < 6){
            errorMessage = "Conform Password must be 6 character long"
        }

        if (errorMessage != null){
            binding.conformPasswordLayout.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validatePasswordAndConformPassword() : Boolean {
        var errorMessage:String? = null
        val password = binding.passwordEdt.text.toString()
        val confirmPassword = binding.conformPasswordEdt.text.toString()
        if (password != confirmPassword){
            errorMessage = "confirmPassword doesn't match with password"
        }

        if (errorMessage != null){
            binding.passwordLayout.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    override fun onClick(view: View?) {
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null){
            when(view.id){
                R.id.fullNameEdt -> {
                    if (hasFocus){
                        if (binding.fullNameLayout.isErrorEnabled){
                            binding.fullNameLayout.isErrorEnabled = false
                        }
                    }else{
                        validateFullName()
                    }
                }

                R.id.emailEdt -> {
                    if (hasFocus){
                        if (binding.emailLayout.isErrorEnabled){
                            binding.emailLayout.isErrorEnabled = false
                        }
                    }else{
                        if (validateEmail()){
                            //do validation for it's uniqueness
                        }
                    }
                }

                R.id.passwordEdt -> {
                    if (hasFocus){
                        if (binding.passwordLayout.isErrorEnabled){
                            binding.passwordLayout.isErrorEnabled = false
                        }
                    }else{
                        if (validatePassword() && binding.conformPasswordEdt.text!!.isNotEmpty() && validateConformPassword() &&
                            validatePasswordAndConformPassword()){
                            if (binding.conformPasswordLayout.isErrorEnabled){
                                binding.conformPasswordLayout.isErrorEnabled = false
                            }
                            binding.conformPasswordLayout.apply {
                                setStartIconDrawable(R.drawable.ic_check)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }

                R.id.conformPasswordEdt -> {
                    if (hasFocus){
                        if (binding.conformPasswordLayout.isErrorEnabled){
                            binding.conformPasswordLayout.isErrorEnabled = false
                        }
                    }else{
                        if (validateConformPassword() && validatePassword() && validatePasswordAndConformPassword()){
                            if (binding.conformPasswordLayout.isErrorEnabled){
                                binding.conformPasswordLayout.isErrorEnabled = false
                            }
                            binding.conformPasswordLayout.apply {
                                setStartIconDrawable(R.drawable.ic_check)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }


            }
        }
    }

    override fun onKey(view: View?, event: Int, eventKey: KeyEvent?): Boolean {
        return false
    }
}