package com.nurbk.ps.demochat.adapter


import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.AsyncListDiffer
import com.nurbk.ps.demochat.R
import com.nurbk.ps.demochat.model.Group
import com.nurbk.ps.demochat.model.User
import com.nurbk.ps.demochat.other.decodeImage
import kotlinx.android.synthetic.main.item_list_users.view.*

class GroupAdapter :
    BaseUserAdapter(R.layout.item_list_users) {
    override val differ = AsyncListDiffer(this, diffCallback)
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = dataList[position] as Group
        holder.itemView.apply {
            isOnline.visibility = View.GONE
            image.setImageBitmap(decodeImage(user.image))
            usernaem.text = user.name
            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(user, false)
                }
            }
        }
    }
}