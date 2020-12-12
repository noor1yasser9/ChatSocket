package com.nurbk.ps.demochat.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nurbk.ps.demochat.R
import com.nurbk.ps.demochat.adapter.GroupAdapter
import com.nurbk.ps.demochat.databinding.FragmentListBinding
import com.nurbk.ps.demochat.model.Group
import com.nurbk.ps.demochat.model.User
import com.nurbk.ps.demochat.network.SocketManager
import com.nurbk.ps.demochat.other.*
import com.nurbk.ps.demochat.ui.viewmodel.GroupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.reflect.Type

class ListGroupFragment : Fragment() {

    private var mSocket: Socket? = null

    private lateinit var mBinding: FragmentListBinding
    private val groupAdapter = GroupAdapter()
    private lateinit var user: User

    private val addGroup by lazy { AddGroupDialogFragment.getInstance(arrayListOf()) }
    private val viewMode by lazy { ViewModelProvider(requireActivity())[GroupViewModel::class.java] }
    private val userString by lazy {
        ConfigUser.getInstance(requireContext())!!.getPreferences()!!.getString(DATA_USER_NAME, "")
    }

    private var isDataShow = true
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentListBinding.inflate(layoutInflater, container, false).apply {
            executePendingBindings()
        }

        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        user = Gson().fromJson(userString, User::class.java)

        mBinding.rcDataUser.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        mSocket = SocketManager.getInstance(requireContext())!!.getSocket()

        requireActivity().title = "All Group"

        mSocket!!.on("AllGroup", onGroupList)

        isDataShow = savedInstanceState?.getBoolean(IS_CONNECTING) ?: true

        if (isDataShow)
            mSocket!!.emit("AllGroup", true)


        groupAdapter.setItemClickListener { user, b ->
            findNavController()
                .navigate(
                    R.id.globalActionToChatFragment,
                    Bundle().apply {
                        putParcelable(USER_RECIPIENT, user as Group)
                        putString(TYPE_CHAT, GROUP_FRAGMENT)
                    }
                )
        }

        viewMode.getAllGroup().observe(viewLifecycleOwner) {
            groupAdapter.apply {
                dataList = it
            }
        }

    }


    private var onGroupList = Emitter.Listener { args ->
        GlobalScope.launch(Dispatchers.Main) {
            val length = args.size
            if (length == 0) {
                return@launch
            }
            val groupsListToken: Type = object : TypeToken<List<Group>>() {}.type
            val groups = Gson().fromJson<List<Group>>(args[0].toString(), groupsListToken)
            val myGroup = ArrayList<Group>()
            groups.map { groupsUser ->
                groupsUser.userGroup.map { userid ->
                    if (userid == user.id) {
                        myGroup.add(groupsUser)
                        viewMode.addListGroup(myGroup)
                    }
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_group, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.createGroup -> {
                if (!addGroup!!.isAdded)
                    addGroup!!.show(requireActivity().supportFragmentManager, "")
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_CONNECTING, false)
    }

}