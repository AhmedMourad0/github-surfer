package dev.ahmedmourad.githubsurfer.users.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.view.updatePadding
import com.facebook.shimmer.ShimmerFrameLayout
import dev.ahmedmourad.githubsurfer.R
import dev.ahmedmourad.githubsurfer.utils.dp

private fun createRootScrollView(context: Context): ScrollView {
    return ScrollView(context)
}

private fun createRootShimmerFrameLayout(context: Context): ShimmerFrameLayout {
    return View.inflate(context, R.layout.content_shimmer_container, null) as ShimmerFrameLayout
}

private fun createShimmerLinearLayout(context: Context): LinearLayout {
    return LinearLayout(context).apply {
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
}

fun createCombinedShimmerLayout(context: Context): ScrollView {
    val list = createShimmerLinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        updatePadding(top = dp(8), bottom = dp(8))
        repeat(20) {
            addView(createShimmerCombinedItemLayout(context))
        }
    }
    val shimmer = createRootShimmerFrameLayout(context).apply {
        addView(list)
    }
    return createRootScrollView(context).apply {
        addView(shimmer)
        visibility = View.VISIBLE
    }
}
