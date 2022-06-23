package com.karthee.chatapp.fragments.myprofile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.karthee.chatapp.R
import com.karthee.chatapp.databinding.AlertLogoutBinding
import com.karthee.chatapp.databinding.FMyProfileBinding
import com.karthee.chatapp.db.ChatUserDatabase
import com.karthee.chatapp.utils.*
import com.karthee.chatapp.views.CustomProgressView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class FMyProfile : Fragment(R.layout.f_my_profile) {

    private lateinit var binding: FMyProfileBinding

    @Inject
    lateinit var preferenec: MPreference

    @Inject
    lateinit var db: ChatUserDatabase

    private lateinit var dialog: Dialog

    private val viewModel: FMyProfileViewModel by viewModels()

    private lateinit var context: Activity

    private var progressView: CustomProgressView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FMyProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context = requireActivity()
        progressView = CustomProgressView(context)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.imageProfile.setOnClickListener {
             ImageUtils.askPermission(this)
        }
        binding.btnSaveChanges.setOnClickListener {
            val newName = viewModel.userName.value
            val about = viewModel.about.value
            val image=viewModel.imageUrl.value
            when {
                viewModel.isUploading.value!! -> context.toast("Profile picture is uploading!")
                newName.isNullOrBlank() -> context.toast("User name can't be empty!")
                else -> {
                    context.window.decorView.clearFocus()
                    viewModel.saveChanges(newName,about ?: "" ,image ?: "")
                }
            }
        }
        binding.btnLogout.setOnClickListener {
            dialog.show()
        }
        initDialog()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.profileUpdateState.observe(viewLifecycleOwner, {
            if (it is LoadState.OnLoading) {
                progressView?.show()
            } else
                progressView?.dismiss()
        })
    }

    private fun initDialog() {
        try {
            dialog = Dialog(requireContext())
            val layoutBinder = AlertLogoutBinding.inflate(layoutInflater)
            dialog.setContentView(layoutBinder.root)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutBinder.txtOk.setOnClickListener {
                dialog.dismiss()
                UserUtils.logOut(requireActivity(), preferenec, db)
            }
            layoutBinder.txtCancel.setOnClickListener {
                dialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}