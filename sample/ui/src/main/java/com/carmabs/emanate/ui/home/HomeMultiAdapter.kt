package com.carmabs.emanate.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.carmabs.domain.model.Role
import com.carmabs.domain.model.User
import com.carmabs.emanate.android.ui.recycler.EmaMultiRecyclerAdapter
import com.carmabs.emanate.android.ui.recycler.EmaViewHolder
import com.carmabs.emanate.presentation.extension.fullName
import com.carmabs.emanate.presentation.extension.initials
import com.carmabs.emanate.sample.ui.R
import com.carmabs.emanate.sample.ui.databinding.HomeLayoutItemAdminBinding
import com.carmabs.emanate.sample.ui.databinding.HomeLayoutItemUserBinding

class HomeMultiAdapter : EmaMultiRecyclerAdapter<User>() {

    override fun ViewBinding.bind(item: User, viewType: Int, holder: EmaViewHolder<User>, payloads: MutableList<Any>) {
        when (Role.entries[viewType]) {
            Role.ADMIN -> {
                (this as HomeLayoutItemAdminBinding).apply {
                    tvHomeItemAdminAvatar.text = item.initials
                    tvHomeItemAdmin.text = item.fullName
                }
            }

            Role.BASIC -> {
                (this as HomeLayoutItemUserBinding).apply {
                    tvHomeItemUserAvatar.text = item.initials
                    tvHomeItemUser.text = item.fullName
                }
            }
        }
    }

    override fun createMultiViewHolder(view: ViewGroup, viewType: Int): EmaAdapterMultiViewHolder {
        val viewBinding = when (Role.entries[viewType]) {
            Role.ADMIN -> HomeLayoutItemAdminBinding.inflate(LayoutInflater.from(view.context), view, false)
            Role.BASIC -> HomeLayoutItemUserBinding.inflate(LayoutInflater.from(view.context), view, false)
        }

        return EmaAdapterMultiViewHolder(viewBinding, viewType)
    }

    override fun getItemViewType(position: Int): Int = currentList[position].role.ordinal
}
