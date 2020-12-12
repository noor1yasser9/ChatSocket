package com.nurbk.ps.demochat.ui.dialog

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import com.nurbk.ps.demochat.R
import com.nurbk.ps.demochat.databinding.DialogEditPasswordBinding
import com.nurbk.ps.demochat.model.User
import com.nurbk.ps.demochat.other.ConfigUser
import com.nurbk.ps.demochat.other.DATA_USER_NAME
import com.nurbk.ps.demochat.other.SING_IN
import org.json.JSONObject

class EditPasswordDialog(val onUpdate: OnUpdatePassword) : DialogFragment() {

    private lateinit var mBinding: DialogEditPasswordBinding
    private val userString by lazy {
        ConfigUser.getInstance(requireContext())!!.getPreferences()!!.getString(DATA_USER_NAME, "")
    }
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_Dialog_Alert);

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogEditPasswordBinding.inflate(inflater, container, false)
            .apply {
                executePendingBindings()
            }

        user = Gson().fromJson(userString, User::class.java)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.btnCancel.setOnClickListener {
            dismiss()
        }


        mBinding.btnEditPasswrod.setOnClickListener {
            val oldPassword = mBinding.txtOldPassword.text.toString()
            val newPassword = mBinding.txtNewPassword.text.toString()
            val confPassword = mBinding.txtConfPassword.text.toString()

            when {
                TextUtils.isEmpty(oldPassword) -> {
                    mBinding.txtOldPassword.error = getString(R.string.errorRequired)
                    mBinding.txtOldPassword.requestFocus()
                    return@setOnClickListener
                }
                user.password != oldPassword -> {
                    mBinding.txtOldPassword.error = getString(R.string.errorPassword)
                    mBinding.txtOldPassword.requestFocus()
                    return@setOnClickListener
                }
                TextUtils.isEmpty(newPassword) -> {
                    mBinding.txtNewPassword.error = getString(R.string.errorRequired)
                    mBinding.txtNewPassword.requestFocus()
                    return@setOnClickListener
                }
                newPassword.length < 8 -> {
                    mBinding.txtNewPassword.error = getString(R.string.errorPasswordShort)
                    mBinding.txtNewPassword.requestFocus()
                    return@setOnClickListener
                }
                TextUtils.isEmpty(confPassword) -> {
                    mBinding.txtConfPassword.error = getString(R.string.errorRequired)
                    mBinding.txtConfPassword.requestFocus()
                    return@setOnClickListener
                }
                newPassword != confPassword -> {
                    mBinding.txtConfPassword.error = getString(R.string.errorPassword)
                    mBinding.txtConfPassword.requestFocus()
                    return@setOnClickListener
                }
                else -> {
                    onUpdate.onUpdate(newPassword).also {
                        dismiss()
                    }

                }
            }

        }

    }


    interface OnUpdatePassword {
        fun onUpdate(password: String)
    }

}