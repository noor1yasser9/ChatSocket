package com.nurbk.ps.demochat.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.nurbk.ps.demochat.R
import com.nurbk.ps.demochat.databinding.FragmentAuthLoginBinding
import com.nurbk.ps.demochat.model.User
import com.nurbk.ps.demochat.network.SocketManager
import com.nurbk.ps.demochat.other.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*


class LoginAuthFragment : Fragment() {

    lateinit var mBinding: FragmentAuthLoginBinding

    private var mSocket: Socket? = null

    private var id = ""

    private val configUser by lazy {
        ConfigUser.getInstance(requireContext())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAuthLoginBinding.inflate(layoutInflater, container, false)
            .apply { executePendingBindings() }
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        requireActivity().title = REGISTRATION

        mSocket = SocketManager.getInstance(requireContext())!!.getSocket()
        mSocket!!.on(SING_IN, onSingIn)
        val dataShared = configUser!!.getPreferences()
        id = dataShared!!.getString(ID_DEVi, "")!!



        mBinding.txtUsername.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                mBinding.btnSave.isEnabled = charSequence.toString().trim { it <= ' ' }
                    .isNotEmpty()
            }

            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) = Unit

            override fun afterTextChanged(editable: Editable) = Unit
        })

        mBinding.btnSave.setOnClickListener {
            val userId = JSONObject()
            userId.put(User.USERNAME, mBinding.txtUsername.text.toString())
            userId.put(User.PASSWORD, mBinding.txtPassword.text.toString())

            mSocket!!.emit(
                SING_IN, id,
                userId
            )
        }


        mBinding.txtSingUp.setOnClickListener {
            findNavController().navigate(
                R.id.singUpAuthFragment
            )
        }

    }


    private var onSingIn = Emitter.Listener { args ->
        GlobalScope.launch(Dispatchers.Main) {
            val length = args.size
            if (length == 0) {
                return@launch
            }
            try {
                if (id == args[0].toString()) {
                    configUser!!.getEditor()!!.apply {
                        putString(DATA_USER_NAME, args[1].toString())
                        putBoolean(IS_LOGIN, true)
                        apply()
                    }
                    findNavController().navigate(R.id.action_loginAuthFragment_to_mainFragment)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}