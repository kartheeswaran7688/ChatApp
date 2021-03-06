package com.karthee.chatapp.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.CollectionReference
import com.karthee.chatapp.R
import com.karthee.chatapp.databinding.FLoginEmailBinding
import com.karthee.chatapp.ui.activities.SharedViewModel
import com.karthee.chatapp.utils.*
import com.karthee.chatapp.views.CustomProgressView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {


    private lateinit var binding: FLoginEmailBinding

    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var progressView: CustomProgressView?=null

    private val viewModel by activityViewModels<LoginEmailViewModel>()

    @Inject
    lateinit var userCollection: CollectionReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = FLoginEmailBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressView = CustomProgressView(requireContext())
        setDataInView()
        subscribeObservers()
    }

    private fun setDataInView() {
        binding.viewmodel = viewModel
        binding.btnSignin.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            Utils.closeKeyBoard(requireActivity())
            viewModel.setProgress(true)
            viewModel.login(email,password)
        }
        binding.btnRegister.setOnClickListener {
            Utils.closeKeyBoard(requireActivity())
            val email = binding.editEmail.text.toString()
            val password = binding.edtPassword.text.toString()
            viewModel.setProgress(true)
            viewModel.register(email,password)
//            binding.editEmail.text.clear()
//            binding.edtPassword.text.clear()
        }
    }

    private fun subscribeObservers() {
        try {

            viewModel.getProgress().observe(viewLifecycleOwner) {
                progressView?.toggle(it)
            }


            viewModel.getFailed().observe(viewLifecycleOwner) {
                progressView?.dismiss()
                Toast.makeText(context,"Login Failed ",Toast.LENGTH_SHORT).show()
            }

            viewModel.getTaskResult().observe(viewLifecycleOwner) { taskId ->
                viewModel.setProgress(false)
                if(taskId.isSuccessful) {
                    viewModel.saveLoginDetailsToPref()
                    findNavController().navigate(R.id.action_FLogIn_to_FProfile)
                    //findNavController().navigate(R.id.action_FLogIn_to_FSingleChatHome)
                } else {
                    Toast.makeText(context,"Authentication Failed ",Toast.LENGTH_SHORT).show()
                }
            }

            viewModel.getRegisterTaskResult().observe(viewLifecycleOwner) { taskId ->
                viewModel.setProgress(false)
                if(taskId.isSuccessful) {
                    /*val email = binding.editEmail.text.toString()
                    val password = binding.edtPassword.text.toString()
                    viewModel.setProgress(true)
                    viewModel.register( email,password)*/
                     viewModel.saveLoginDetailsToPref()
                    findNavController().navigate(R.id.action_FLogIn_to_FProfile)
                } else {
                    Toast.makeText(context,"Authentication Failed ",Toast.LENGTH_SHORT).show()
                }
            }

            viewModel.userProfileGot.observe(viewLifecycleOwner) { success ->
                if (success) {
                    requireActivity().toastLong("Authenticated successfully using Instant verification")
                    findNavController().navigate(R.id.action_FLogIn_to_FProfile)
                }
            }

            viewModel.profileUpdateState.observe(viewLifecycleOwner){
                when(it) {
                    is LoadState.OnLoading -> {
                        viewModel.setProgress(true)
                    }
                    is LoadState.OnFailure -> {
                        viewModel.setProgress(false)
                    }
                    is LoadState.OnSuccess-> {
                        viewModel.setProgress(false)
                        findNavController().navigate(R.id.action_FLogIn_to_FSingleChatHome)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        try {
            progressView?.dismissIfShowing()
            super.onDestroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}