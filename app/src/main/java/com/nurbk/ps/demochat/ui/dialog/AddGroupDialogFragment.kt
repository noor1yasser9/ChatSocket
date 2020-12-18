package com.nurbk.ps.demochat.ui.dialog

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.nurbk.ps.demochat.R
import com.nurbk.ps.demochat.adapter.AddGroupAdapter
import com.nurbk.ps.demochat.databinding.DialogGroupUserBinding
import com.nurbk.ps.demochat.model.Group
import com.nurbk.ps.demochat.model.User
import com.nurbk.ps.demochat.network.SocketManager
import com.nurbk.ps.demochat.other.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class AddGroupDialogFragment private constructor(var data: List<User>) : DialogFragment() {


    private lateinit var mBinding: DialogGroupUserBinding
    private val userAdapter = AddGroupAdapter()
    private var mSocket: Socket? = null
    private lateinit var user: User
    private var userString: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_Dialog_Alert);
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DialogGroupUserBinding.inflate(inflater, container, false)
        mSocket = SocketManager.getInstance(requireContext())!!.getSocket()

        return mBinding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        userString = ConfigUser.getInstance(requireContext())!!.getPreferences()!!
            .getString(DATA_USER_NAME, "")!!
        user = Gson().fromJson(userString, User::class.java)


        userAdapter.dataList = data
        arrayGroup.add(user.id)


        mBinding.rcDataUser.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        mSocket!!.on(GROUP_FRAGMENT) {
            arrayGroup.clear()
            mBinding.txtUsername.post {
                mBinding.txtUsername.text!!.clear()
            }
            imageGroup = ""
            dismiss()
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

        userAdapter.setItemClickListener { user, b ->

            if (b) {
                arrayGroup.add((user as User).id)
            } else {
                arrayGroup.remove((user as User).id)
            }
        }

        mBinding.btnSave.setOnClickListener {
            val name = mBinding.txtUsername.text.toString()
            when {
                arrayGroup.size <= 1-> {
                    Snackbar.make(
                        requireView(),
                        "There must be at least two people in the group",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                TextUtils.isEmpty(name) -> {
                    mBinding.txtUsername.error = getString(R.string.errorRequired)
                    mBinding.txtUsername.requestFocus()
                    return@setOnClickListener
                }
                else -> {
                    val group = JSONObject()
                    val array = JSONArray()
                    for (user in arrayGroup) {
                        array.put(user)
                    }
                    group.put(Group.NAME, name)
                    group.put(Group.USER_GROUP, array)
                    group.put(Group.ID, UUID.randomUUID().toString())
                    group.put(Group.IMAGE, imageGroup)

                    mSocket!!.emit(
                        GROUP_FRAGMENT, group
                    )
                }
            }


        }
    }

    private var arrayGroup = ArrayList<String>()
    private var imageGroup = ""

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
            imageGroup = imageToBase64(bitmap)


        }
    }

    companion object {
        var instance: AddGroupDialogFragment? = null
        fun getInstance(data: ArrayList<User>): AddGroupDialogFragment? {
            if (instance == null) {
                instance = AddGroupDialogFragment(data)
            }
            return instance
        }
    }
}