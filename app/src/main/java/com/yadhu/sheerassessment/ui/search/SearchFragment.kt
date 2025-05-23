package com.yadhu.sheerassessment.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.toRoute
import com.bumptech.glide.Glide
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.yadhu.sheerassessment.R
import com.yadhu.sheerassessment.model.user.User
import com.yadhu.sheerassessment.repository.network.NetworkRepositoryImpl
import com.yadhu.sheerassessment.ui.MainActivity
import com.yadhu.sheerassessment.ui.Search
import com.yadhu.sheerassessment.ui.UserList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "SearchFragment"

class SearchFragment: Fragment() {

    private lateinit var shimmerContainer: ShimmerFrameLayout
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

        getArgumentsReceived()
        setupToolbar()

        shimmerContainer = view.findViewById(R.id.search_shimmer_view_container)
        contentGroup = view.findViewById(R.id.content_group)
        tvSearchMessage = view.findViewById(R.id.tv_search_message)
        ivAvatar = view.findViewById(R.id.iv_search_avatar)
        tvName = view.findViewById(R.id.tv_search_name)
        tvUserName = view.findViewById(R.id.tv_search_username)
        tvDescription = view.findViewById(R.id.tv_search_description)
        btnFollower = view.findViewById(R.id.btn_search_follower_count)
        btnFollowing = view.findViewById(R.id.btn_search_following_count)

        setupShimmer()
        setupUserInteractions()
        setupObservers()
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
            val tag = btnFollower.tag as Int
            if (tag == 0) {
                showToastMessage(getString(R.string.follower_no_one))
                return@setOnClickListener
            }

            navController.navigate(route = UserList(tvUserName.text.toString(), true))
        }

        btnFollowing.setOnClickListener {
            val tag = btnFollowing.tag as Int
            if (tag == 0) {
                showToastMessage(getString(R.string.following_no_one))
                return@setOnClickListener
            }

            navController.navigate(route = UserList(tvUserName.text.toString(), false))
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            mSearchViewModel.uiState.collectLatest { state ->
                when (state) {
                    is SearchUIState.NoData -> {
                        if (state.isLoading) {
                            shimmerContainer.startShimmer()
                            showLoading()
                        } else {
                            shimmerContainer.stopShimmer()
                            Log.d(TAG, "Message:: ${state.message}")
                            showMessage(getString(R.string.no_data_text))
                        }
                    }

                    is SearchUIState.HasData -> {
                        shimmerContainer.stopShimmer()
                        shimmerContainer.visibility = View.GONE

                        showContent()
                        setContent(state.data)
                    }
                }

            }
        }
    }

    private fun setupToolbar() {
        if (findNavController().previousBackStackEntry != null) {
            (activity as MainActivity).changeToolbarTitleVisibility(true)
            (activity as MainActivity).setActionBarTitle(mSearchViewModel.searchQuery.value)
            return
        } else {
            (activity as MainActivity).changeToolbarTitleVisibility(false)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_search, menu)

                val searchMenuItem = menu.findItem(R.id.action_search)
                val searchView = searchMenuItem.actionView as SearchView
                searchView.maxWidth = Int.MAX_VALUE
                searchView.setIconifiedByDefault(false)
                searchView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_searchbar)

                searchView.queryHint = "Search username"
                if (mSearchViewModel.searchQuery.value.isNotEmpty()) {
                    searchView.onActionViewExpanded()
                    searchView.setQuery(mSearchViewModel.searchQuery.value, true)
                }

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
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
        btnFollower.tag = user.followers
        btnFollowing.text = getString(R.string.following_format, user.following.toString())
        btnFollowing.tag = user.following
    }

    private fun showLoading() {
        contentGroup.visibility = View.GONE
        tvSearchMessage.visibility = View.GONE
        shimmerContainer.visibility = View.VISIBLE
    }

    private fun showMessage(message: String?) {
        // For now it is setting to "No data" as there is not error code check required
        message?.let {
            tvSearchMessage.text = it
            contentGroup.visibility = View.GONE
            shimmerContainer.visibility = View.GONE
            tvSearchMessage.visibility = View.VISIBLE

        }
    }

    private fun showContent() {
        contentGroup.visibility = View.VISIBLE
        tvSearchMessage.visibility = View.GONE
        shimmerContainer.visibility = View.GONE
    }

    private fun setupShimmer() {
        val shimmerBuilder = Shimmer.AlphaHighlightBuilder()
        shimmerBuilder.setDirection(Shimmer.Direction.TOP_TO_BOTTOM).setTilt(0f)
        shimmerContainer.setShimmer(shimmerBuilder.build())
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}