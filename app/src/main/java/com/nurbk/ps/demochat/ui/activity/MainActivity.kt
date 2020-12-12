package com.nurbk.ps.demochat.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.github.nkzawa.socketio.client.Socket
import com.nurbk.ps.demochat.R
import com.nurbk.ps.demochat.databinding.ActivityMainBinding
import com.nurbk.ps.demochat.network.SocketManager
import com.nurbk.ps.demochat.other.ConfigUser
import com.nurbk.ps.demochat.other.IS_LOGIN
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding


    private var mSocket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


        mSocket = SocketManager.getInstance(this)!!.getSocket()




        mSocket!!.on(Socket.EVENT_CONNECT_ERROR) {}
        mSocket!!.on(Socket.EVENT_CONNECT_TIMEOUT) {}
        mSocket!!.on(Socket.EVENT_CONNECT) {}
        mSocket!!.on(Socket.EVENT_DISCONNECT) {}



        setSupportActionBar(mBinding.msgToolbar)


//        val navHostFragment = supportFragmentManager
//            .findFragmentById(R.id.fragment_nav_host_home) as NavHostFragment?
//
//
//
//        NavigationUI.setupWithNavController(
//            mBinding.msgToolbar,
//            navHostFragment!!.navController,
//            AppBarConfiguration.Builder(navHostFragment.navController.graph).build()
//        )


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_nav_host_home) as NavHostFragment?
        NavigationUI.setupWithNavController(
            mBinding.bottomNavigationView,
            Objects.requireNonNull(navHostFragment)!!.navController
        )
        val navController = navHostFragment!!.navController

        val appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()


        NavigationUI.setupWithNavController(
            mBinding.msgToolbar, navController, appBarConfiguration
        )



        navHostFragment.navController.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, arguments: Bundle? ->
            when (destination.id) {
                R.id.loginAuthFragment, R.id.singUpAuthFragment -> {
                    mBinding.msgToolbar.visibility = View.GONE
                    mBinding.bottomNavigationView.visibility = View.GONE
                }
                R.id.mainFragment, R.id.profileFragment -> {
                    mBinding.msgToolbar.navigationIcon = null
                    mBinding.bottomNavigationView.visibility = View.VISIBLE
                    mBinding.msgToolbar.visibility = View.VISIBLE
                }
                else -> {
                    mBinding.msgToolbar.visibility = View.VISIBLE
                    mBinding.msgToolbar.visibility = View.GONE
                }
            }
        }

        val dataShared = ConfigUser.getInstance(this)!!.getPreferences()

        if (dataShared!!.getBoolean(IS_LOGIN, false)) {
            val graph = navHostFragment.navController
                .navInflater.inflate(R.navigation.nav_main)
            graph.startDestination = R.id.mainFragment
            navHostFragment.navController.graph = graph
        }


    }


}