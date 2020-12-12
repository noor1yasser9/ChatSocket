package com.nurbk.ps.demochat.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
import com.nurbk.ps.demochat.R
import com.nurbk.ps.demochat.databinding.ActivityMainBinding
import com.nurbk.ps.demochat.model.User
import com.nurbk.ps.demochat.network.SocketManager
import com.nurbk.ps.demochat.other.*
import org.json.JSONObject
import java.lang.Exception
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private val userString by lazy {
        ConfigUser.getInstance(this)!!.getPreferences()!!.getString(DATA_USER_NAME, "")
    }
    private lateinit var user: User
    private var mSocket: Socket? = null
    private var isDataShow = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


        mSocket = SocketManager.getInstance(this)!!.getSocket()




        mSocket!!.on(Socket.EVENT_CONNECT_ERROR) {}
        mSocket!!.on(Socket.EVENT_CONNECT_TIMEOUT) {}
        mSocket!!.on(Socket.EVENT_CONNECT) {}
        mSocket!!.on(Socket.EVENT_DISCONNECT) {}

        user = try {

            Gson().fromJson(userString, User::class.java)
        } catch (e: Exception) {
            User()
        }


        setSupportActionBar(mBinding.msgToolbar)

        isDataShow = savedInstanceState?.getBoolean(IS_CONNECTING) ?: true
        if (isDataShow) {
            mSocket!!.emit(GET_ALL_USER, true)
            mSocket!!.emit(UPDATE_DATA, JSONObject().apply {
                put(User.ID, user.id)
                put(User.IS_ONLINE, true)
            })
        }

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
                    mBinding.bottomNavigationView.visibility = View.GONE
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_CONNECTING, false)
    }


    override fun onDestroy() {
        if (mSocket != null)
            mSocket!!.emit(UPDATE_DATA, JSONObject().apply {
                put(User.ID, user.id)
                put(User.IS_ONLINE, false)
            })
        super.onDestroy()
    }

}