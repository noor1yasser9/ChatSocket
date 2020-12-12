package com.nurbk.ps.demochat.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nurbk.ps.demochat.other.ID
import com.nurbk.ps.demochat.R
import com.nurbk.ps.demochat.databinding.ItemRecipientMessageBinding
import com.nurbk.ps.demochat.databinding.ItemSenderMessageBinding
import com.nurbk.ps.demochat.databinding.ItemUserConnectedBinding
import com.nurbk.ps.demochat.model.Message
import com.nurbk.ps.demochat.other.decodeImage

class MessageAdapter(val data: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val LAYOUT_SENDER = 0
    private val LAYOUT_RECIPIENT = 1
    private val LAYOUT_CONNECTED = 2


    class ConnectedViewHolder(private val item: ItemUserConnectedBinding) :
        RecyclerView.ViewHolder(item.root) {
        fun bind(message: Message) {
            item.messageBody.text = message.username
        }
    }

    class SenderViewHolder(private val item: ItemSenderMessageBinding) :
        RecyclerView.ViewHolder(item.root) {
        fun bind(message: Message) {
            if (message.message.length <= 200) {
                item.messageBody.visibility = View.VISIBLE
                item.messageBody.text = message.message
                item.cardImage.visibility = View.GONE
            } else {
                item.messageBody.visibility = View.GONE
                item.cardImage.visibility = View.VISIBLE
                item.imageView.setImageBitmap(decodeImage(message.message));
            }
        }
    }

    class RecipientViewHolder(private val item: ItemRecipientMessageBinding) :
        RecyclerView.ViewHolder(item.root) {
        fun bind(message: Message) {
            item.messageName.text = message.username
            item.image.setImageBitmap(decodeImage(message.imageUser))
            if (message.message.length <= 200) {
                item.messageBody.visibility = View.VISIBLE
                item.messageBody.text = message.message
                item.cardImage.visibility = View.GONE
            } else {
                item.messageBody.visibility = View.GONE
                item.cardImage.visibility = View.VISIBLE
                item.imageView.setImageBitmap(decodeImage(message.message));
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            LAYOUT_CONNECTED -> {
                return ConnectedViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_user_connected,
                        parent,
                        false
                    )
                )
            }
            LAYOUT_SENDER -> {
                return SenderViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_sender_message,
                        parent,
                        false
                    )
                )
            }
            else -> {
                return RecipientViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_recipient_message,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when {
            getItemViewType(position) == LAYOUT_SENDER -> {
                (holder as SenderViewHolder).bind(data[position])
            }
            getItemViewType(position) == LAYOUT_RECIPIENT -> {
                (holder as RecipientViewHolder).bind(data[position])
            }
            getItemViewType(position) == LAYOUT_CONNECTED -> {
                (holder as ConnectedViewHolder).bind(data[position])
            }
        }
    }

    override fun getItemCount() = data.size

    override fun getItemViewType(position: Int): Int {
        data[position].also { message ->
            return when {
                TextUtils.isEmpty(message.message) -> {
                    LAYOUT_CONNECTED
                }
                message.idS == ID -> {
                    LAYOUT_SENDER
                }
                else -> LAYOUT_RECIPIENT
            }
        }

    }


}