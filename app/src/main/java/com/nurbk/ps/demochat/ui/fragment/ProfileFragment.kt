package com.nurbk.ps.demochat.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
import com.nurbk.ps.demochat.databinding.FragmentProfileBinding
import com.nurbk.ps.demochat.model.User
import com.nurbk.ps.demochat.network.SocketManager
import com.nurbk.ps.demochat.other.*
import com.nurbk.ps.demochat.ui.activity.MainActivity
import com.nurbk.ps.demochat.ui.dialog.EditPasswordDialog
import org.json.JSONObject

class ProfileFragment : Fragment(), EditPasswordDialog.OnUpdatePassword {

    private lateinit var mBinding: FragmentProfileBinding
    private val userString by lazy {
        ConfigUser.getInstance(requireContext())!!.getPreferences()!!.getString(DATA_USER_NAME, "")
    }
    private lateinit var user: User
    private val json = JSONObject()
    private var mSocket: Socket? = null
    private val dialogPassword = EditPasswordDialog(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentProfileBinding.inflate(inflater, container, false).apply {
            executePendingBindings()
        }
        mSocket = SocketManager.getInstance(requireContext())!!.getSocket()

        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = Gson().fromJson(userString, User::class.java)

        mBinding.image.setImageBitmap(
            decodeImage(user.image)
        )
        mBinding.txtProfileName.text = user.name
        json.put(User.ID, user.id)
        json.put(User.NAME, user.name)
        json.put(User.IS_ONLINE, true)
        json.put(User.EMAIL, user.email)
        json.put(User.PASSWORD, user.password)
        json.put(User.IMAGE, user.image)

        requireActivity().title="Profile"

        mBinding.btnLogOut.setOnClickListener {
            ConfigUser.getInstance(requireContext())!!.getEditor()!!.clear().apply()
            requireActivity().finish()
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        mBinding.btnEditPassword.setOnClickListener {
            if (!dialogPassword.isAdded)
                dialogPassword.show(requireActivity().supportFragmentManager, "")
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

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = TYPE_INTENT_IMAGE
        startActivityForResult(intent, REQUEST_IMAGE_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (
            requestCode == REQUEST_IMAGE_CODE &&
            resultCode == Activity.RESULT_OK
        ) {
            val imageUri = data!!.data
            val bitmap =
                MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)

            json.put(User.IMAGE, imageToBase64(bitmap))
            mSocket!!.emit("dataProfile", json)

            ConfigUser.getInstance(requireContext())!!.getEditor()!!.apply {
                putString(DATA_USER_NAME, json.toString())
                apply()
            }
        }
    }

    override fun onUpdate(password: String) {
        json.put(User.PASSWORD, password)
        mSocket!!.emit("dataProfile", json)
        ConfigUser.getInstance(requireContext())!!.getEditor()!!.apply {
            putString(DATA_USER_NAME, json.toString())
            apply()
        }
    }
}