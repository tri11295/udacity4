package com.udacity.project4

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentLoginBinding
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import org.koin.android.ext.android.inject

class LoginFragment : BaseFragment() {
    override val _viewModel: LoginViewModel by inject()
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login, container, false
        )
        binding.btnLogin.setOnClickListener {
            launchSignInFlow()
        }

//        observeAuthenticationState()
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Log.i(
                    "tri11",
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToReminderListFragment())
            } else {
                Toast.makeText(
                    requireContext(),
                    "Sign in unsuccessful ${response?.error?.errorCode}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.i("tri11", "Sign in unsuccessful ${response?.error?.localizedMessage}")
            }
        }
    }

    private fun observeAuthenticationState() {
        _viewModel.authenticationState.observe(viewLifecycleOwner) { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                }
                else -> {
                }
            }
        }
    }


    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email
        // If users choose to register with their email,
        // they will need to create a password as well
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), SIGN_IN_RESULT_CODE
        )
    }

    companion object {
        const val SIGN_IN_RESULT_CODE = 1001
    }
}