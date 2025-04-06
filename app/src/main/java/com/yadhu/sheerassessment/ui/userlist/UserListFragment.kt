package com.yadhu.sheerassessment.ui.userlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.toRoute
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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


private const val TAG = "UserListFragment"

class UserListFragment : Fragment() {

    private lateinit var mShimmerContainer: ShimmerFrameLayout

    private val mUserAdapter: UserAdapter = UserAdapter(::onUserClick)

    private val mUserListViewModel: UserListViewModel by viewModels {
        UserListViewModel.getUserListViewModelFactory(networkRepository = NetworkRepositoryImpl.getNetworkInstance())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_userlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mShimmerContainer = view.findViewById(R.id.shimmer_view_container)
        val rvUserList: RecyclerView = view.findViewById(R.id.rv_user_list)

        setArgumentsReceived()
        setupToolbar()
        setupShimmer()

        rvUserList.apply {
            adapter = mUserAdapter
            layoutManager = LinearLayoutManager(this.context)
        }

        setupUIStateObserver()
    }

    private fun setupToolbar() {
        (activity as MainActivity).changeToolbarTitleVisibility(true)
        (activity as MainActivity).setActionBarTitle(mUserListViewModel.username.value)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        })
    }

    private fun setArgumentsReceived() {
        val userList = findNavController().getBackStackEntry<UserList>().toRoute<UserList>()
        mUserListViewModel.updateUserName(Pair(userList.username!!, userList.isFollower))
    }

    private fun setupUIStateObserver() {
        lifecycleScope.launch {
            mUserListViewModel.users.collect { users ->
                mUserAdapter.submitData(users)
            }
        }

        lifecycleScope.launch {
            mUserAdapter.loadStateFlow.collectLatest { loadStates ->
                if (loadStates.refresh is LoadState.Loading) {
                    mShimmerContainer.startShimmer()
                    mShimmerContainer.visibility = View.VISIBLE
                } else if (loadStates.refresh is LoadState.NotLoading) {
                    mShimmerContainer.stopShimmer()
                    mShimmerContainer.visibility = View.GONE

                }
            }
        }
    }

    private fun setupShimmer() {
        val shimmerBuilder = Shimmer.AlphaHighlightBuilder()
        shimmerBuilder.setDirection(Shimmer.Direction.TOP_TO_BOTTOM).setTilt(0f)
        mShimmerContainer.setShimmer(shimmerBuilder.build())
    }

    private fun onUserClick(user: User) {
        val navController = findNavController()
        navController.navigate(route = Search(user.login))
    }
}