package com.nurbk.ps.demochat.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
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
            val username = mBinding.txtUsername.text.toString()
            val password = mBinding.txtPassword.text.toString()

            when {
                TextUtils.isEmpty(username) -> {
                    mBinding.txtUsername.error = getString(R.string.errorRequired)
                    mBinding.txtUsername.requestFocus()
                    return@setOnClickListener
                }
                !Patterns.EMAIL_ADDRESS.matcher(username).matches() -> {
                    mBinding.txtUsername.error = getString(R.string.email)
                    mBinding.txtUsername.requestFocus()
                    return@setOnClickListener
                }
                TextUtils.isEmpty(password) -> {
                    mBinding.txtPassword.error = getString(R.string.errorRequired)
                    mBinding.txtPassword.requestFocus()
                    return@setOnClickListener
                }
                password.length < 8 -> {
                    mBinding.txtPassword.error = getString(R.string.errorPasswordShort)
                    mBinding.txtPassword.requestFocus()
                    return@setOnClickListener
                }
                else -> {
                    val userId = JSONObject()
                    userId.put(User.EMAIL, username)
                    userId.put(User.PASSWORD, password)
                    mSocket!!.emit(SING_IN, id, userId)
                }
            }


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
                    val user = Gson().fromJson(args[1].toString(), User::class.java)
                    findNavController().navigate(R.id.action_loginAuthFragment_to_mainFragment)
                        .also {
                            mSocket!!.emit(GET_ALL_USER, true)
                            mSocket!!.emit(UPDATE_DATA, JSONObject().apply {
                                put(User.ID, user.id)
                                put(User.IS_ONLINE, true)
                            })
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}