package com.example.bookshelf

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshelf.ui.user.User

class UserAdapter(val context: Context, val friendsList: ArrayList<User>): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private lateinit var mListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    class UserViewHolder(itemView: View, listener: onItemClickListener): RecyclerView.ViewHolder(itemView){
        val txtFriends = itemView.findViewById<TextView>(R.id.txtFriends)
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.user, parent, false)
        return UserViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        var currentUser = friendsList[position]
        holder.txtFriends.text = currentUser.nickname!!.uppercase()
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }
}