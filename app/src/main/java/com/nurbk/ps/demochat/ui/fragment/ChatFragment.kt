package com.nurbk.ps.demochat.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.nkzawa.emitter.Emitter
import com.google.gson.Gson
import com.nurbk.ps.demochat.adapter.MessageAdapter
import com.nurbk.ps.demochat.databinding.FragmentChatBinding
import com.nurbk.ps.demochat.model.Group
import com.nurbk.ps.demochat.model.Message
import com.nurbk.ps.demochat.model.User
import com.nurbk.ps.demochat.network.SocketManager
import com.nurbk.ps.demochat.other.*
import com.nurbk.ps.demochat.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class ChatFragment : Fragment() {

    private lateinit var mBinding: FragmentChatBinding
    private lateinit var user: User
    private lateinit var userRecipient: String
    private lateinit var group: Group

    private val mSocket by lazy { SocketManager.getInstance(requireContext())!!.getSocket() }
    private val adapterMessage by lazy { MessageAdapter(arrayListOf()) }
    private val userString by lazy {
        ConfigUser.getInstance(requireContext())!!.getPreferences()!!.getString(DATA_USER_NAME, "")
    }
    private val viewModelChat by lazy { ViewModelProvider(this)[ChatViewModel::class.java] }
    private val bundle by lazy { requireArguments() }

    private var type = ""
    private var isDataShow = true
    private var isConnecting = true
    private var startTyping = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentChatBinding.inflate(layoutInflater, container, false).apply {
            executePendingBindings()
        }

        if (savedInstanceState != null) {
            isConnecting = bundle.getBoolean(IS_CONNECTING, false)
        }

        user = Gson().fromJson(userString, User::class.java)
        ID = user.id

        val userId = JSONObject()
        type = bundle.getString(TYPE_CHAT)!!
        if (type == USER_FRAGMENT) {
            userRecipient = bundle.getString(USER_ID)!!
            userId.put(Message.USERNAME, "${user.email} $CONNECTED")
            if (isConnecting)
                mSocket!!
                    .emit(
                        CHAT_MESSAGE,
                        "${user.id}${userRecipient}",
                        userId
                    )
        } else {
            group = bundle.getParcelable(USER_RECIPIENT)!!
            userId.put(Message.USERNAME, "${user.email} $CONNECTED")
            if (isConnecting)
                mSocket!!.emit(CHAT_MESSAGE, group.id, userId)
        }

        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = CHAT
        mSocket!!.on(CHAT_MESSAGE, onSend)
        mSocket!!.on(ON_TYPING, onTyping)


        mBinding.btnSend.setOnClickListener {
            sendMessage()
        }

        mBinding.recyclerViewMessage.apply {
            adapter = adapterMessage
        }

        mBinding.btnDataSend.setOnClickListener {
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

        onTypeButtonEnable()


        setHasOptionsMenu(false)

        viewModelChat.getAllChat(
            if (type == USER_FRAGMENT)
                "${user.id}${userRecipient}" else group.id
        )
            .observe(viewLifecycleOwner) {
                if (isDataShow) {
                    isDataShow = false
                    adapterMessage.apply {
                        this.data.apply {
                            addAll(it)
                        }
                        notifyDataSetChanged()
                    }
                }
            }

        isConnecting = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_CONNECTING, isConnecting)
    }

    private fun sendMessage() {
        val message: String = mBinding.msgText.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(message)) {
            return
        }
        sendDataJSON(message = message, typing = false, keyEmit = CHAT_MESSAGE)
        mBinding.msgText.text.clear()

    }


    private var onSend = Emitter.Listener { args ->
        GlobalScope.launch(Dispatchers.Main) {
            if (type == USER_FRAGMENT) {
                if ("${user.id}${userRecipient}" == args[0].toString() ||
                    "${userRecipient}${user.id}" == args[0].toString()
                ) {
                    try {
                        recData(data = args[1].toString(), args[0].toString(), 1)

                    } catch (e: Exception) {
                        return@launch
                    }
                }
            } else {
                if (group.id == args[0].toString()) {
                    recData(data = args[1].toString(), args[0].toString(), 2)
                }
            }
        }
    }


    private var onTyping = Emitter.Listener { args ->
        GlobalScope.launch(Dispatchers.Main) {
            if (type == USER_FRAGMENT) {
                if ("${user.id}${userRecipient}" == args[0].toString() ||
                    "${userRecipient}${user.id}" == args[0].toString()
                ) {

                    try {
                        onTypingUser(args = args[1].toString())

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            } else {
                if (group.id == args[0].toString()) {
                    onTypingUser(args = args[1].toString())
                }
            }
        }
    }

    var isTyping = false
    private fun onTypeButtonEnable() {

        mBinding.msgText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (!isTyping) {
                    sendDataJSON(typing = true, keyEmit = ON_TYPING)
                    isTyping = true
                }
            }

            override fun afterTextChanged(editable: Editable) {
                sendDataJSON(keyEmit = ON_TYPING, typing = false)
            }
        })
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
            sendDataJSON(message = imageToBase64(bitmap), keyEmit = CHAT_MESSAGE, typing = false)

        }
    }


    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = TYPE_INTENT_IMAGE
        startActivityForResult(intent, REQUEST_IMAGE_CODE)
    }


    private fun sendDataJSON(
        message: String = "",
        typing: Boolean,
        keyEmit: String = ""
    ) {
        val json = JSONObject()
        json.put(Message.TYPING, typing)
        json.put(Message.USERNAME, user.name)
        if (!TextUtils.isEmpty(message))
            json.put(Message.MESSAGE, message)
        json.put(Message.IMAGE_USER, user.image)
        json.put(Message.ID, user.id)
        if (type == USER_FRAGMENT) {
            mSocket!!.emit(keyEmit, "${user.id}${userRecipient}", json)
        } else {
            mSocket!!.emit(
                keyEmit,
                group.id,
                json
            )
        }
    }

    private fun recData(data: String, id: String, type: Int) {
        val message = Gson().fromJson(data, Message::class.java)
        message.idUser = if (type == 1) "${user.id}${userRecipient}" else id
        message.time = Calendar.getInstance().timeInMillis.toString()
        viewModelChat.insert(message)
        adapterMessage.apply {
            this.data.add(
                0,
                message
            )
            mBinding.recyclerViewMessage.scrollTo(0, adapterMessage.itemCount - 1);
            notifyDataSetChanged()
        }


    }

    private fun onTypingUser(args: String) {
        val userTyping = Gson().fromJson(args, Message::class.java)

        if (userTyping.idS != ID) {
            if (userTyping.isTyping) {
                if (!startTyping) {
                    startTyping = true
                    GlobalScope.launch(Dispatchers.Main) {
                        mBinding.txtTypingUser.text = userTyping.username
                        mBinding.group.visibility = View.VISIBLE
                        delay(4000)
                        mBinding.group.visibility = View.GONE
                        startTyping = false
                        isTyping = false
                    }
                }
            }
        }
    }


}
