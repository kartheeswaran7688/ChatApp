package com.karthee.chatapp.fragments.create_group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.karthee.chatapp.R
import com.karthee.chatapp.databinding.FCreateGroupBinding
import com.karthee.chatapp.db.daos.ChatUserDao
import com.karthee.chatapp.db.data.ChatUser
import com.karthee.chatapp.db.data.Group
import com.karthee.chatapp.fragments.add_group_members.AdAddMembers
import com.karthee.chatapp.utils.*
import com.karthee.chatapp.views.CustomProgressView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FCreateGroup : Fragment() {

    @Inject
    lateinit var chatUserDao: ChatUserDao

    @Inject
    lateinit var preference: MPreference

    private val viewModel: CreateGroupViewModel by viewModels()

    private lateinit var binding: FCreateGroupBinding

    val args by navArgs<FCreateGroupArgs>()

    private lateinit var memberList: List<ChatUser>

    private var progressView: CustomProgressView?=null

    private val adMembers: AdAddMembers by lazy {
        AdAddMembers(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding=FCreateGroupBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner=viewLifecycleOwner
        binding.viewmodel=viewModel
        progressView= CustomProgressView(requireContext())
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.fab.setOnClickListener {
          validate()
        }
        setDataInView()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.groupCreateStatus.observe(viewLifecycleOwner,{
            when (it) {
                is LoadState.OnSuccess -> {
                    if (findNavController().isValidDestination(R.id.FCreateGroup)) {
                        progressView?.dismiss()
                        val group=it.data as Group
                        preference.setCurrentGroup(group.id)
                        val action=FCreateGroupDirections.actionFCreateGroupToFGroupChat(group)
                        findNavController().navigate(action)
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
    }

    private fun validate() {
        val groupName=viewModel.groupName.value.toString().trim()
        if (groupName.isNotEmpty() && !viewModel.progressProPic.value!!)
            viewModel.createGroup(memberList as ArrayList<ChatUser>)
    }

    private fun setDataInView() {
        binding.edtGroupName.requestFocus()
        Utils.showSoftKeyboard(requireActivity(),binding.edtGroupName)
        memberList=args.memberList.toList().map {
            it.isSelected=false
            it
        }
        val memberCount=memberList.size
        binding.memberCount=if(memberCount==1) "$memberCount member" else "$memberCount members"
        binding.listMembers.adapter = adMembers
        adMembers.addRestorePolicy()
        adMembers.submitList(memberList)
    }

    override fun onDestroy() {
        super.onDestroy()
        progressView?.dismissIfShowing()
        Utils.closeKeyBoard(requireActivity())
    }

}