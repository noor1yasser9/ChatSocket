package com.nurbk.ps.demochat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nurbk.ps.demochat.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var mBinding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentProfileBinding.inflate(inflater, container, false).apply {
            executePendingBindings()
        }

        return mBinding.root
    }




}