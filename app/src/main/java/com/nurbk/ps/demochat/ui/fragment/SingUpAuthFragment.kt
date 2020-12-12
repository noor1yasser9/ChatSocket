package com.nurbk.ps.demochat.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.nurbk.ps.demochat.databinding.FragmentAuthSingUpBinding
import com.nurbk.ps.demochat.model.Message
import com.nurbk.ps.demochat.model.User
import com.nurbk.ps.demochat.network.SocketManager
import com.nurbk.ps.demochat.other.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*

class SingUpAuthFragment : Fragment() {

    private lateinit var mBinding: FragmentAuthSingUpBinding

    private var mSocket: Socket? = null
    private var imageUser: String? = null

    private var id = ""
    private val configUser by lazy {
        ConfigUser.getInstance(requireContext())!!
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentAuthSingUpBinding.inflate(inflater, container, false).apply {
            executePendingBindings()
        }
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        mSocket = SocketManager.getInstance(requireContext())!!.getSocket()



        mSocket!!.on(SING_UP, onSingUp)



        mBinding.txtLogin.setOnClickListener {
            findNavController().navigateUp()
        }

        mBinding.btnSave.setOnClickListener {
            val username = mBinding.txtUsername.text.toString()
            val password = mBinding.txtPassword.text.toString()
            val password2 = mBinding.txtPassword2.text.toString()
            when {
                TextUtils.isEmpty(username) -> {
                    return@setOnClickListener
                }
                TextUtils.isEmpty(password) -> {
                    return@setOnClickListener
                }
                TextUtils.isEmpty(password2) -> {
                    return@setOnClickListener
                }
                password != password2 -> {
                    return@setOnClickListener
                }
                TextUtils.isEmpty(imageUser) -> {
                    return@setOnClickListener
                }
                else -> {
                    configUser.getEditor()!!
                        .putString(ID_DEVi, UUID.randomUUID().toString()).apply()
                    id = configUser
                        .getPreferences()!!.getString(ID_DEVi, "")!!

                    val user = JSONObject()
                    user.put(User.USERNAME, username)
                    user.put(User.PASSWORD, password)
                    user.put(User.ID, UUID.randomUUID().toString())
                    user.put(User.IMAGE, imageUser)
                    user.put(User.IS_ONLINE, false)

                    mSocket!!.emit(
                        SING_UP, id, user
                    )
                }
            }
        }

        mBinding.cardView.setOnClickListener {
            permission(
                requireContext(),
                arrayListOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                selectImage()
            }
        }

    }


    private var onSingUp = Emitter.Listener { args ->
        GlobalScope.launch(Dispatchers.Main) {
            val length = args.size
            if (length == 0) {
                return@launch
            }
            try {
                if (id == args[0].toString()
                ) {
                    if (args[1].toString().toBoolean())
                        findNavController().navigateUp()
                    else {
                        Toast.makeText(requireContext(), "User Theer is Exiss", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            } catch (e: Exception) {
            }


        }
    }


    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = TYPE_INTENT_IMAGE
        startActivityForResult(intent, REQUEST_IMAGE_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CODE &&
            resultCode == Activity.RESULT_OK
        ) {

            if (!TextUtils.isEmpty(mBinding.txtUsername.text)) {
                mBinding.btnSave.isEnabled = true
            }

            val imageUri = data!!.data
            mBinding.image.setImageURI(imageUri)

            val bitmap =
                MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
            imageUser = imageToBase64(bitmap)


        }
    }

}