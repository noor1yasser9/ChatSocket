package com.nurbk.ps.demochat.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nurbk.ps.demochat.R
import com.nurbk.ps.demochat.adapter.UserAdapter
import com.nurbk.ps.demochat.databinding.FragmentListBinding
import com.nurbk.ps.demochat.model.User
import com.nurbk.ps.demochat.network.SocketManager
import com.nurbk.ps.demochat.other.*
import com.nurbk.ps.demochat.ui.viewmodel.UsersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception
import java.lang.reflect.Type

class ListUserFragment : Fragment() {

    private lateinit var mBinding: FragmentListBinding
    private lateinit var user: User

    private val userAdapter = UserAdapter()
    private val addGroup by lazy { AddGroupDialogFragment.getInstance(arrayListOf()) }
    private val viewMode by lazy { ViewModelProvider(requireActivity())[UsersViewModel::class.java] }


    private var mSocket: Socket? = null
    private var userString: String = ""
    private var isDataShow = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentListBinding.inflate(layoutInflater, container, false)
            .apply { executePendingBindings() }
        userString = ConfigUser.getInstance(requireContext())!!.getPreferences()!!
            .getString(DATA_USER_NAME, "")!!
        user = try {
            Gson().fromJson(userString, User::class.java)
        } catch (e: Exception) {
            User()
        }
        mSocket = SocketManager.getInstance(requireContext())!!.getSocket()
        mSocket!!.on(GET_ALL_USER, onUserList)

        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isDataShow = savedInstanceState?.getBoolean(IS_CONNECTING) ?: true
        if (isDataShow) {
            mSocket!!.emit(GET_ALL_USER, true)
            mSocket!!.emit(UPDATE_DATA, JSONObject().apply {
                put(User.ID, user.id)
                put(User.IS_ONLINE, true)
            })
        }

        setHasOptionsMenu(false)
        mBinding.rcDataUser.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        requireActivity().title = "All User"

        viewMode.getAllUser().observe(viewLifecycleOwner) {
            userAdapter.apply {
                dataList = it
                addGroup!!.data = it
            }
        }

        userAdapter.setItemClickListener { user, _ ->
            findNavController()
                .navigate(
                    R.id.globalActionToChatFragment,
                    Bundle().apply {
                        this.putString(USER_ID, (user as User).id)
                        this.putString(TYPE_CHAT, USER_FRAGMENT)
                    }
                )
        }
    }


    private var onUserList = Emitter.Listener { args ->
        GlobalScope.launch(Dispatchers.Main) {
            val length = args.size
            if (length == 0) {
                return@launch
            }
            val userListToken: Type = object : TypeToken<ArrayList<User>>() {}.type
            val userList =
                Gson().fromJson<ArrayList<User>>(
                    args[0].toString(),
                    userListToken
                )
            user.isOnline = true
            userList.remove(user)
            viewMode.addListUsers(userList)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_CONNECTING, false)
    }

}