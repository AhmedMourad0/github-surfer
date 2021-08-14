package dev.ahmedmourad.githubsurfer.users.ui

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.google.android.material.progressindicator.CircularProgressIndicator
import dev.ahmedmourad.githubsurfer.R
import dev.ahmedmourad.githubsurfer.utils.dp

fun createLoadingProgressBarContainer(context: Context): FrameLayout {
    return FrameLayout(context).apply {
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        updatePadding(
            top = dp(16),
            bottom = dp(16),
            left = dp(8),
            right = dp(8)
        )
    }
}

fun createLoadingProgressBar(context: Context): CircularProgressIndicator {
    return CircularProgressIndicator(context).apply {
        isIndeterminate = true
        setIndicatorColor(ContextCompat.getColor(context, R.color.purple_500))
        layoutParams = FrameLayout.LayoutParams(dp(8), dp(8)).apply {
            gravity = Gravity.CENTER
        }
    }
}
