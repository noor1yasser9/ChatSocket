package com.nurbk.ps.demochat.adapter


import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.AsyncListDiffer
import com.nurbk.ps.demochat.R
import com.nurbk.ps.demochat.model.User
import com.nurbk.ps.demochat.other.decodeImage
import kotlinx.android.synthetic.main.item_list_users.view.*

class UserAdapter :
    BaseUserAdapter(R.layout.item_list_users) {
    override val differ = AsyncListDiffer(this, diffCallback)
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = dataList[position] as User
        holder.itemView.apply {
            image.setImageBitmap(decodeImage(user.image))
            usernaem.text = user.name
            isOnline.setCardBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    if (user.isOnline) R.color.colorGreen else R.color.grey,
                    null
                )
            )
            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(user, false)
                }
            }
        }
    }
}