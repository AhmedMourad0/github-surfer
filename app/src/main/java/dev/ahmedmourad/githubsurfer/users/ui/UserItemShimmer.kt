package dev.ahmedmourad.githubsurfer.users.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.CHAIN_PACKED
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.view.updateMargins
import androidx.core.view.updateMarginsRelative
import androidx.core.view.updatePaddingRelative
import de.hdodenhof.circleimageview.CircleImageView
import dev.ahmedmourad.githubsurfer.R
import dev.ahmedmourad.githubsurfer.utils.dp

fun createShimmerItemRootConstraintLayout(context: Context): ConstraintLayout {
    return ConstraintLayout(context).apply {
        id = R.id.user_item_shimmer_root
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            updateMargins(
                left = dp(8),
                right = dp(8),
                top = dp(8),
                bottom = dp(8)
            )
            updatePaddingRelative(
                start = dp(16),
                top = dp(10),
                bottom = dp(10),
                end = dp(12)
            )
        }
    }
}

fun createShimmerItemAvatarView(context: Context): View {
    return CircleImageView(context).apply {
        id = R.id.user_item_shimmer_avatar
        this.setImageDrawable(ColorDrawable(Color.parseColor("#545252")))
        layoutParams = ConstraintLayout.LayoutParams(dp(60), dp(60)).apply {
            topToTop = PARENT_ID
            startToStart = PARENT_ID
            bottomToBottom = PARENT_ID
            dimensionRatio = "H,1:1"
        }
    }
}

fun createShimmerItemUsernameView(context: Context): View {
    return View(context).apply {
        id = R.id.user_item_shimmer_username
        background = ColorDrawable(Color.parseColor("#545252"))
        layoutParams = ConstraintLayout.LayoutParams(0, dp(26)).apply {
            topToTop = PARENT_ID
            startToEnd = R.id.user_item_shimmer_avatar
            bottomToBottom = PARENT_ID
            endToEnd = PARENT_ID
            updateMarginsRelative(
                start = dp(12),
                end = dp(54)
            )
        }
    }
}

fun createShimmerCombinedItemLayout(context: Context): ConstraintLayout {
    return createShimmerItemRootConstraintLayout(context).apply {
        addView(createShimmerItemAvatarView(context))
        addView(createShimmerItemUsernameView(context))
    }
}
