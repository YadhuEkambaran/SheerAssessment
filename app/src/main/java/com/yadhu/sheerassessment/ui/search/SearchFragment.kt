package com.yadhu.sheerassessment.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.Group
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.toRoute
import com.bumptech.glide.Glide
import com.yadhu.sheerassessment.R
import com.yadhu.sheerassessment.model.user.User
import com.yadhu.sheerassessment.repository.network.NetworkRepositoryImpl
import com.yadhu.sheerassessment.ui.Search
import com.yadhu.sheerassessment.ui.UserList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "SearchFragment"

class SearchFragment: Fragment() {

    private lateinit var contentGroup: Group
    private lateinit var tvSearchMessage: AppCompatTextView
    private lateinit var ivAvatar: AppCompatImageView
    private lateinit var tvName: AppCompatTextView
    private lateinit var tvUserName: AppCompatTextView
    private lateinit var tvDescription: AppCompatTextView
    private lateinit var btnFollower: AppCompatButton
    private lateinit var btnFollowing: AppCompatButton

    private val mSearchViewModel: SearchViewModel by viewModels {
        SearchViewModel.getSearchViewModelFactory(NetworkRepositoryImpl.getNetworkInstance())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchView()

        contentGroup = view.findViewById(R.id.content_group)
        tvSearchMessage = view.findViewById(R.id.tv_search_message)
        ivAvatar = view.findViewById(R.id.iv_search_avatar)
        tvName = view.findViewById(R.id.tv_search_name)
        tvUserName = view.findViewById(R.id.tv_search_username)
        tvDescription = view.findViewById(R.id.tv_search_description)
        btnFollower = view.findViewById(R.id.btn_search_follower_count)
        btnFollowing = view.findViewById(R.id.btn_search_following_count)

        getArgumentsReceived()
        setupUserInteractions()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Query:: ${mSearchViewModel.searchQuery.value}")
    }

    private fun getArgumentsReceived() {
        val search = findNavController().getBackStackEntry<Search>().toRoute<Search>()
        search.username?.let {
            if (it.isNotEmpty()) {
                mSearchViewModel.updateSearchQuery(it)
            }
        }
    }

    private fun setupUserInteractions() {
        val navController = findNavController()

        btnFollower.setOnClickListener {
            navController.navigate(route = UserList(tvUserName.text.toString(), true))
        }

        btnFollowing.setOnClickListener {
            navController.navigate(route = UserList(tvUserName.text.toString(), false))
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            mSearchViewModel.uiState.collectLatest { state ->
                when (state) {
                    is SearchUIState.NoData -> {
                        changeContentVisibility(state = false)
                        Log.d(TAG, "Message:: ${state.message}")
                        setMessage(state.code, getString(R.string.no_data_text))
                    }

                    is SearchUIState.HasData -> {
                        if (state.isLoading) {
                            changeContentVisibility(state = false)
                            setMessage(100, getString(R.string.loading_text))
                        } else {
                            changeContentVisibility(state = true)
                            setContent(state.data)
                        }
                    }
                }

            }
        }
    }

    private fun setupSearchView() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_search, menu)

                val searchMenuItem = menu.findItem(R.id.action_search)
                val searchView = searchMenuItem.actionView as SearchView

                searchView.queryHint = "Search"
                searchView.setQuery(mSearchViewModel.searchQuery.value, true)

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        mSearchViewModel.updateSearchQuery(query)
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        newText?.let {
                            if (it.isEmpty()) mSearchViewModel.updateSearchQuery("")
                        }
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun changeContentVisibility(state: Boolean) {
        if (state) {
            contentGroup.visibility = View.VISIBLE
            tvSearchMessage.visibility = View.GONE
        } else {
            contentGroup.visibility = View.GONE
            tvSearchMessage.visibility = View.VISIBLE
        }
    }

    private fun setContent(user: User) {
        Glide.with(this)
            .load(user.avatar_url)
            .circleCrop()
            .into(ivAvatar)

        tvName.text = user.name
        tvUserName.text = user.login
        tvDescription.text = user.bio
        btnFollower.text = getString(R.string.follower_format, user.followers.toString())
        btnFollowing.text = getString(R.string.follower_format, user.following.toString())
    }

    private fun setMessage(code: Int?, message: String?) {
        // For now it is setting to "No data" as there is not error code check required
        message?.let {
            tvSearchMessage.text = it
        }
    }
}