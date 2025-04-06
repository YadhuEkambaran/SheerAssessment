package com.yadhu.sheerassessment.ui.userlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.yadhu.sheerassessment.R
import com.yadhu.sheerassessment.model.user.User

class UserAdapter(private val onUserClick: (User) -> Unit): PagingDataAdapter<User, UserAdapter.UserViewHolder>(UserDiffUtil()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.single_user, parent, false)
        return UserViewHolder(view, onUserClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUser(getItem(position)!!)
    }

    class UserViewHolder(itemView: View, private val onUserClick: (User) -> Unit): ViewHolder(itemView) {
        private val ivAvatar: AppCompatImageView = itemView.findViewById(R.id.iv_user_avatar)
        private var tvName: AppCompatTextView = itemView.findViewById(R.id.tv_user_name)

        fun setUser(user: User) {
            Glide.with(ivAvatar.context)
                .load(user.avatar_url)
                .circleCrop()
                .into(ivAvatar)

            tvName.text = user.login
            itemView.setOnClickListener { onUserClick(user) }
        }
    }

    class UserDiffUtil: DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

    }
}