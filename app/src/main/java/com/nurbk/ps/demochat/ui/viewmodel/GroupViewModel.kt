package com.nurbk.ps.demochat.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nurbk.ps.demochat.model.Group
import com.nurbk.ps.demochat.model.User

class GroupViewModel(application: Application) : AndroidViewModel(application) {
    private val _getGroupViewModel = MutableLiveData<List<Group>>()
    private val getGroupViewModel: LiveData<List<Group>> = _getGroupViewModel
    fun addListGroup(listUser: List<Group>) {
        _getGroupViewModel.postValue(listUser)
    }

    fun getAllGroup() = getGroupViewModel
}