package dev.ahmedmourad.githubsurfer.users.users

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dev.ahmedmourad.githubsurfer.R
import dev.ahmedmourad.githubsurfer.core.users.model.SimpleUser
import dev.ahmedmourad.githubsurfer.users.users.FindUsersViewModel.Companion.INITIAL_SINCE
import dev.ahmedmourad.githubsurfer.users.ui.*
import dev.ahmedmourad.githubsurfer.utils.useNegativeColors
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.IllegalArgumentException

private const val TYPE_LOADING = 0
private const val TYPE_USER = 1

private typealias OnUserSelectedListener = (user: SimpleUser) -> Unit
private typealias OnRequestMoreItems = (nextSince: Long) -> Unit

class FindUsersAdapter(
    private val context: Context,
    private val items: MutableStateFlow<List<SimpleUser>>,
    private val onRequestMoreItems: OnRequestMoreItems,
    private val onUserSelectedListener: OnUserSelectedListener
) : RecyclerView.Adapter<BindableViewHolder<SimpleUser>>() {

    private var since = INITIAL_SINCE
    private var nextSince = INITIAL_SINCE

    fun submitData(items: List<SimpleUser>, since: Long?) {
        this.since = since ?: this.since
        this.nextSince = items.maxOfOrNull { it.id.value } ?: INITIAL_SINCE
        val newItems = if (this.since <= INITIAL_SINCE) items else this.items.value + items
        val diff = DiffUtil.calculateDiff(Differ(this.items.value, newItems))
        this.items.value = newItems
        diff.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position <= items.value.lastIndex) {
            TYPE_USER
        } else {
            TYPE_LOADING
        }
    }

    override fun getItemCount(): Int {
        return if (items.value.isNotEmpty()) items.value.size + 1 else 0
    }

    override fun onCreateViewHolder(
        container: ViewGroup,
        viewType: Int
    ): BindableViewHolder<SimpleUser> {
        return when (viewType) {
            TYPE_LOADING -> createLoadingViewHolder()
            TYPE_USER -> createUserViewHolder()
            else -> throw IllegalArgumentException("$viewType is not a supported view type")
        }
    }

    private fun createUserViewHolder(): UserViewHolder {
        val avatar = createItemAvatarImageView(context)
        val username = createItemUsernameTextView(context)
        val notesIndicator = createItemNotesIndicatorImageView(context)
        val root = createItemRootConstraintLayout(context).apply {
            addView(avatar)
            addView(username)
            addView(notesIndicator)
        }
        return UserViewHolder(
            root = root,
            username = username,
            avatar = avatar,
            notesIndicator = notesIndicator
        )
    }

    private fun createLoadingViewHolder(): LoadingViewHolder {
        val progressBar = createLoadingProgressBar(context)
        val root = createLoadingProgressBarContainer(context).apply {
            addView(progressBar)
        }
        return LoadingViewHolder(view = root)
    }

    override fun onBindViewHolder(
        holder: BindableViewHolder<SimpleUser>,
        position: Int
    ) {
        if (items.value.isNotEmpty() && items.value.lastIndex - position < 8) {
            onRequestMoreItems(nextSince)
        }
        holder.bind(position, items.value.getOrNull(position))
    }

    fun refreshItem(item: SimpleUser): Int {
        val position = this.items.value.indexOfFirst { it.id == item.id }
        val newItems = this.items.value.toMutableList().apply { this[position] = item }
        val diff = DiffUtil.calculateDiff(Differ(this.items.value, newItems))
        this.items.value = newItems
        diff.dispatchUpdatesTo(this)
        return position
    }

    inner class UserViewHolder(
        root: View,
        private val username: TextView,
        private val avatar: ImageView,
        private val notesIndicator: ImageView
    ) : BindableViewHolder<SimpleUser>(root) {

        private val glide = Glide.with(context)

        override fun bind(position: Int, item: SimpleUser?) {
            item ?: return
            username.text = item.login
            notesIndicator.visibility = if (item.hasNotes) View.VISIBLE else View.GONE
            glide.load(item.avatarUrl)
                .placeholder(ColorDrawable(ContextCompat.getColor(context, R.color.placeholder)))
                .error(ColorDrawable(ContextCompat.getColor(context, R.color.error)))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(avatar)
            avatar.useNegativeColors((position + 1) % 4 == 0)
            itemView.setOnClickListener {
                onUserSelectedListener(item)
            }
        }
    }

    inner class LoadingViewHolder(view: View) : BindableViewHolder<SimpleUser>(view)

    private class Differ(
        val old: List<SimpleUser>,
        val new: List<SimpleUser>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size
        override fun areItemsTheSame(oldIndex: Int, newIndex: Int): Boolean {
            return old[oldIndex].id == new[newIndex].id
        }
        override fun areContentsTheSame(oldIndex: Int, newIndex: Int): Boolean {
            return old[oldIndex] == new[newIndex]
        }
    }
}

abstract class BindableViewHolder<T : Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    open fun bind(position: Int, item: T?) { }
}
