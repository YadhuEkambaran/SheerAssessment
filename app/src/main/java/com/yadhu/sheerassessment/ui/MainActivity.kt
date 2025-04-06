package com.yadhu.sheerassessment.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.createGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.fragment
import androidx.navigation.ui.NavigationUI
import com.yadhu.sheerassessment.R
import com.yadhu.sheerassessment.ui.search.SearchFragment
import com.yadhu.sheerassessment.ui.userlist.UserListFragment


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        setupNavController()
    }

    private fun setupNavController() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.graph = navController.createGraph(
            startDestination = Search(username = "")
        ) {
            fragment<SearchFragment, Search> {

            }

            fragment<UserListFragment, UserList> {

            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun changeToolbarTitleVisibility(state: Boolean) {
        supportActionBar?.run {
            setDisplayShowTitleEnabled(state)
            setDisplayHomeAsUpEnabled(state)
        }
    }

    fun setActionBarTitle(title: String) {
        supportActionBar?.let {
            it.title = title.capitalizeFirstChar()
        }
    }

    private fun String.capitalizeFirstChar(): String {
        return replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
    }
}