package com.karthee.chatapp.fragments.countries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.karthee.chatapp.R
import com.karthee.chatapp.databinding.FCountriesBinding
import com.karthee.chatapp.ui.activities.SharedViewModel
import com.karthee.chatapp.utils.ItemClickListener
import com.karthee.chatapp.utils.Utils
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FCountries : Fragment(), ItemClickListener {

    private lateinit var binding: FCountriesBinding

    private lateinit var recyclerView: RecyclerView

    private lateinit var searchView: SearchView

    private lateinit var adCountry: AdCountries

    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = FCountriesBinding.inflate(layoutInflater, container, false)
        setHasOptionsMenu(true)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.listCountry
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        setDataInView()
    }

    private fun setDataInView() {
        try {
            binding.toolbar.inflateMenu(R.menu.menu_search)
            val searchItem: MenuItem? = binding.toolbar.menu.findItem(R.id.action_search)
            searchView = searchItem?.actionView as SearchView
            searchView.apply {
                maxWidth = Integer.MAX_VALUE
                queryHint = getString(R.string.txt_search)
            }
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adCountry.filter(newText.toString())
                    return true
                }
            })
            AdCountries.itemClickListener = this
            adCountry = AdCountries()
            adCountry.setData()
            recyclerView.adapter = adCountry
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onItemClicked(v: View, position: Int) {
        findNavController().popBackStack()
        sharedViewModel.setCountry(adCountry.countries[position])
    }
}