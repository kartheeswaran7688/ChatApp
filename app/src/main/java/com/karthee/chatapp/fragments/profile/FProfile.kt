package com.karthee.chatapp.fragments.profile

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.CollectionReference
import com.karthee.chatapp.R
import com.karthee.chatapp.databinding.FProfileBinding
import com.karthee.chatapp.models.UserStatus
import com.karthee.chatapp.utils.*
import com.karthee.chatapp.views.CustomProgressView
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class FProfile : Fragment() {

    private lateinit var binding: FProfileBinding

    private lateinit var context: Activity

    @Inject
    lateinit var preference: MPreference

    @Inject
    lateinit var userCollection: CollectionReference

    private var progressView: CustomProgressView? = null

    private val viewModel: ProfileViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = FProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context = requireActivity()
        UserUtils.updatePushToken(context,userCollection,true)
        EventBus.getDefault().post(UserStatus())
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        progressView = CustomProgressView(context)
        binding.fab.setOnClickListener { validate() }
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.profileUpdateState.observe(viewLifecycleOwner, {
            when (it) {
                is LoadState.OnSuccess -> {
                    if (findNavController().isValidDestination(R.id.FProfile)) {
                        progressView?.dismiss()
                        findNavController().navigate(R.id.action_FProfile_to_FSingleChatHome)
                    }
                }
                is LoadState.OnFailure -> {
                    progressView?.dismiss()
                }
                is LoadState.OnLoading -> {
                    progressView?.show()
                }
            }
        })

        viewModel.checkUserNameState.observe(viewLifecycleOwner,{
            when (it) {
                is LoadState.OnFailure -> {
                    progressView?.dismiss()
                }
                is LoadState.OnLoading -> {
                    progressView?.show()
                }
            }
        })
    }

    private fun validate() {
        val name = viewModel.name.value
        if (!name.isNullOrEmpty() && name.length > 1 )
            viewModel.storeProfileData()
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