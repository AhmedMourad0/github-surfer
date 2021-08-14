package dev.ahmedmourad.githubsurfer.users.search

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
import dev.ahmedmourad.githubsurfer.users.ui.*
import dev.ahmedmourad.githubsurfer.utils.useNegativeColors

private typealias OnResultSelectedListener = (user: SimpleUser) -> Unit

class SearchResultsAdapter(
    private val context: Context,
    private val onUserSelectedListener: OnResultSelectedListener
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    private var items = listOf<SimpleUser>()

    fun submitData(items: List<SimpleUser>) {
        val diff = DiffUtil.calculateDiff(Differ(this.items, items))
        this.items = items
        diff.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(
        container: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val avatar = createItemAvatarImageView(context)
        val username = createItemUsernameTextView(context)
        val notesIndicator = createItemNotesIndicatorImageView(context)
        val root = createItemRootConstraintLayout(context).apply {
            addView(avatar)
            addView(username)
            addView(notesIndicator)
        }
        return ViewHolder(
            root = root,
            username = username,
            avatar = avatar,
            notesIndicator = notesIndicator
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(position, items[position])
    }

    fun refreshItem(item: SimpleUser): Int {
        val position = this.items.indexOfFirst { it.id == item.id }
        val newItems = this.items.toMutableList().apply { this[position] = item }
        val diff = DiffUtil.calculateDiff(Differ(this.items, newItems))
        this.items = newItems
        diff.dispatchUpdatesTo(this)
        return position
    }

    inner class ViewHolder(
        root: View,
        private val username: TextView,
        private val avatar: ImageView,
        private val notesIndicator: ImageView
    ) : RecyclerView.ViewHolder(root) {

        private val glide = Glide.with(context)

        fun bind(position: Int, item: SimpleUser) {
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
