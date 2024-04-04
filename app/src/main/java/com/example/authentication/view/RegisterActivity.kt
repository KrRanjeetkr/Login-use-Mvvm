package com.example.authentication.view

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.authentication.R
import com.example.authentication.data.RegisterBody
import com.example.authentication.data.ValidateEmailBody
import com.example.authentication.databinding.ActivityRegisterBinding
import com.example.authentication.repository.AuthRepository
import com.example.authentication.utils.ApiService
import com.example.authentication.utils.VibrateView
import com.example.authentication.view_model.RegisterActivityViewModel
import com.example.authentication.view_model.RegisterActivityViewModelFactory
import java.lang.StringBuilder

class RegisterActivity : AppCompatActivity(), View.OnClickListener,
    View.OnFocusChangeListener, View.OnKeyListener, TextWatcher {

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
        binding.conformPasswordEdt.setOnKeyListener(this)
        binding.conformPasswordEdt.addTextChangedListener(this)
        binding.registerBtn.setOnClickListener(this)

        mViewModel = ViewModelProvider(this, RegisterActivityViewModelFactory(AuthRepository(ApiService.getServices
            ()), application)).get(RegisterActivityViewModel::class.java)

        setupObservers()
    }

    private fun setupObservers() {
        mViewModel.getIsLoading().observe(this){
            binding.progressbar.isVisible = it
        }

        mViewModel.getIsUnique().observe(this){
            if (validateEmail(shouldUpdateView = false)){
                if (it){
                    binding.emailLayout.apply {
                        if (isErrorEnabled) isErrorEnabled = false
                        setStartIconDrawable(R.drawable.ic_check)
                        setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                    }
                }else{
                    binding.emailLayout.apply {
                        if (startIconDrawable != null) startIconDrawable = null
                        isErrorEnabled = true
                        error = "Email is Already Taken"
                    }
                }
            }
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

    private fun validateFullName(shouldVibrateView: Boolean = true) : Boolean {
        var errorMessage: String? = null
        val value: String = binding.fullNameEdt.text.toString()
        if (value.isEmpty()){
            errorMessage = "Full name is required"
        }

        if (errorMessage != null){
            binding.fullNameLayout.apply {
                isErrorEnabled = true
                error = errorMessage
                if (shouldVibrateView) VibrateView.vibrate(this@RegisterActivity, this)
            }
        }
        return errorMessage == null
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
               if (shouldVibrateView) VibrateView.vibrate(this@RegisterActivity, this)
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
                if (shouldVibrateView) VibrateView.vibrate(this@RegisterActivity, this)
            }
        }

        return errorMessage == null
    }

    private fun validateConformPassword(shouldUpdateView: Boolean = true, shouldVibrateView: Boolean = true) : Boolean {
        var errorMessage: String? = null
        val value: String = binding.conformPasswordEdt.text.toString()
        if (value.isEmpty()){
            errorMessage = "Conform Password is required"
        }else if (value.length < 6){
            errorMessage = "Conform Password must be 6 character long"
        }

        if (errorMessage != null && shouldUpdateView){
            binding.conformPasswordLayout.apply {
                isErrorEnabled = true
                error = errorMessage
                if (shouldVibrateView) VibrateView.vibrate(this@RegisterActivity, this)
            }
        }

        return errorMessage == null
    }

    private fun validatePasswordAndConformPassword(shouldUpdateView: Boolean = true, shouldVibrateView: Boolean = true) : Boolean {
        var errorMessage:String? = null
        val password = binding.passwordEdt.text.toString()
        val confirmPassword = binding.conformPasswordEdt.text.toString()
        if (password != confirmPassword){
            errorMessage = "confirmPassword doesn't match with password"
        }

        if (errorMessage != null && shouldUpdateView){
            binding.passwordLayout.apply {
                isErrorEnabled = true
                error = errorMessage
                if (shouldVibrateView) VibrateView.vibrate(this@RegisterActivity, this)
            }
        }

        return errorMessage == null
    }

    override fun onClick(view: View?) {
        if (view != null && view.id == R.id.registerBtn)
            onSubmit()
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
                            mViewModel.validateEmailAddress(ValidateEmailBody(binding.emailEdt.text!!.toString()))
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

    override fun onKey(view: View?, keyCode: Int, eventKey: KeyEvent?): Boolean {
        if (KeyEvent.KEYCODE_ENTER == keyCode && eventKey!!.action == KeyEvent.ACTION_UP){
            // do registration
            onSubmit()
        }

        return false
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (validatePassword(shouldUpdateView = false) && validateConformPassword(shouldUpdateView = false)
            && validatePasswordAndConformPassword(shouldUpdateView = false)){
            binding.conformPasswordLayout.apply {
                if (isErrorEnabled) isErrorEnabled = false
                setStartIconDrawable(R.drawable.ic_check)
                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
            }
        }else{
            if (binding.conformPasswordLayout.startIconDrawable != null)
                binding.conformPasswordLayout.startIconDrawable = null
        }
    }

    override fun afterTextChanged(s: Editable?) {
    }

    private fun onSubmit(){
        if (validate()){
            //make api response
            mViewModel.registerUser(RegisterBody(binding.fullNameEdt.text!!.toString(), binding.emailEdt.text!!.toString(), binding.passwordEdt.text!!.toString()))
        }
    }

    private fun validate() : Boolean{
        var isValid = true

        if (!validateFullName(shouldVibrateView = false)) isValid = false
        if (!validateEmail(shouldVibrateView = false)) isValid = false
        if (!validatePassword(shouldVibrateView = false)) isValid = false
        if (!validateConformPassword(shouldVibrateView = false)) isValid = false
        if (isValid && !validatePasswordAndConformPassword(shouldVibrateView = false)) isValid = false

        if (!isValid) VibrateView.vibrate(this, binding.cardView)

        return isValid
    }

}