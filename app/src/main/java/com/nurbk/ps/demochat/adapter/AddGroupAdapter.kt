package com.nurbk.ps.demochat.adapter

import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.AsyncListDiffer
import com.nurbk.ps.demochat.R
import com.nurbk.ps.demochat.model.User
import com.nurbk.ps.demochat.other.decodeImage
import kotlinx.android.synthetic.main.item_list_group_users.view.*
import kotlinx.android.synthetic.main.item_list_users.view.image
import kotlinx.android.synthetic.main.item_list_users.view.isOnline
import kotlinx.android.synthetic.main.item_list_users.view.usernaem

class AddGroupAdapter :
    BaseUserAdapter(R.layout.item_list_group_users) {


    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val user = dataList[position] as User

        holder.itemView.apply {
            image.setImageBitmap(decodeImage(user.image))
            usernaem.text = user.username
            isOnline.setCardBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    if (user.isOnline) R.color.colorGreen else R.color.grey,
                    null
                )
            )

            btnSelectUser.setOnClickListener {
                Log.e("tttttt", "${btnSelectUser.isChecked}")
                onItemClickListener?.let { click ->
                    click(user, btnSelectUser.isChecked)
                }
            }
        }
    }
}