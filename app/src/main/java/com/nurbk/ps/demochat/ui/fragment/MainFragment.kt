package com.nurbk.ps.demochat.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.viewpager2.widget.ViewPager2
import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.nurbk.ps.demochat.adapter.ViewPagerAdapter
import com.nurbk.ps.demochat.databinding.FragmentMainBinding
import com.nurbk.ps.demochat.model.User
import com.nurbk.ps.demochat.network.SocketManager
import com.nurbk.ps.demochat.other.*
import org.json.JSONObject


class MainFragment : Fragment() {

    private lateinit var mBinding: FragmentMainBinding
    private val userString by lazy {
        ConfigUser.getInstance(requireContext())!!.getPreferences()!!.getString(DATA_USER_NAME, "")
    }
    private lateinit var user: User
    private var mSocket: Socket? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMainBinding.inflate(layoutInflater, container, false).apply {
            executePendingBindings()
        }
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (userString != null)
            user = Gson().fromJson(userString, User::class.java)
        mSocket = SocketManager.getInstance(requireContext())!!.getSocket()




        initViewPage()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    private fun initViewPage() {
        val viewPagerAdapter = ViewPagerAdapter(requireActivity())
        viewPagerAdapter.addFragment(ListUserFragment())
        viewPagerAdapter.addFragment(ListGroupFragment())
        mBinding.pagerHome.apply {
            adapter = viewPagerAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    requireActivity().title = if (position == 0) "All User" else "Group User"
                }
            })
        }

        TabLayoutMediator(
            mBinding.tabLayout, mBinding.pagerHome
        ) { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> {
                    tab.text = USER_FRAGMENT
                    setHasOptionsMenu(false)
                }
                1 -> {
                    tab.text = GROUP_FRAGMENT
                }
            }
        }.attach()

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