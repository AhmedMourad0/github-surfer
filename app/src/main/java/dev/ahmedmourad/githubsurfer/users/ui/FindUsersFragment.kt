package dev.ahmedmourad.githubsurfer.users.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.*
import androidx.core.content.ContextCompat
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dev.ahmedmourad.githubsurfer.R
import dev.ahmedmourad.githubsurfer.utils.dp

fun createRootConstraintLayout(context: Context): ConstraintLayout {
    return ConstraintLayout(context).apply {
        clipChildren = false
        clipToPadding = false
    }
}

fun createAppBar(context: Context): AppBarLayout {
    return (View.inflate(context, R.layout.content_appbar, null) as AppBarLayout).apply {
        id = R.id.user_app_bar
        layoutParams = ConstraintLayout.LayoutParams(0, WRAP_CONTENT).apply {
            topToTop = PARENT_ID
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
        }
    }
}

fun createSwipeToRefresh(context: Context): SwipeRefreshLayout {
    return SwipeRefreshLayout(context).apply {
        setColorSchemeColors(ContextCompat.getColor(context, R.color.purple_500))
        layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
            topToBottom = R.id.user_app_bar
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
            bottomToBottom = PARENT_ID
        }
    }
}

fun createContentFrameLayout(context: Context): FrameLayout {
    return FrameLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
}

fun createErrorView(context: Context): View {
    return View.inflate(context, R.layout.content_error, null).apply {
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        visibility = View.GONE
    }
}

fun createNewDataFab(context: Context): ExtendedFloatingActionButton {
    return ExtendedFloatingActionButton(context).apply {
        setIconResource(R.drawable.ic_refresh)
        visibility = View.GONE
        text = context.getString(R.string.newer_data_available)
        textSize = 10f
        iconSize = dp(20)
        setBackgroundColor(Color.BLACK)
        setTextColor(Color.WHITE)
        iconTint = ColorStateList.valueOf(Color.WHITE)
        layoutParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            bottomToBottom = PARENT_ID
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
            updateMargins(bottom = dp(32))
        }
    }
}

fun createUsersRecyclerView(context: Context): RecyclerView {
    return RecyclerView(context).apply {
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        clipToPadding = false
        updatePadding(top = dp(16), bottom = dp(16))
        visibility = View.GONE
    }
}
