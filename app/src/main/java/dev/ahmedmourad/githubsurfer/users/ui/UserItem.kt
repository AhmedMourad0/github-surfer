package dev.ahmedmourad.githubsurfer.users.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
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

fun createItemRootConstraintLayout(context: Context): ConstraintLayout {
    return ConstraintLayout(context).apply {
        id = R.id.user_item_root
        isClickable = true
        isFocusable = true
        background = ContextCompat.getDrawable(context, R.drawable.user_item_background)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            foreground = ContextCompat.getDrawable(context, R.drawable.user_item_foreground)
        }
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            updateMargins(
                left = dp(8),
                right = dp(8),
                top = dp(4),
                bottom = dp(4)
            )
            updatePaddingRelative(
                start = dp(16),
                top = dp(8),
                bottom = dp(8),
                end = dp(12)
            )
        }
    }
}

fun createItemAvatarImageView(context: Context): ImageView {
    return CircleImageView(context).apply {
        id = R.id.user_item_avatar
        borderWidth = dp(1)
        borderColor = Color.LTGRAY
        layoutParams = ConstraintLayout.LayoutParams(dp(60), dp(60)).apply {
            topToTop = PARENT_ID
            startToStart = PARENT_ID
            bottomToBottom = PARENT_ID
            dimensionRatio = "H,1:1"
        }
    }
}

fun createItemUsernameTextView(context: Context): TextView {
    return TextView(context).apply {
        id = R.id.user_item_username
        setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL)
        textSize = 18f
        setTextColor(Color.BLACK)
        layoutParams = ConstraintLayout.LayoutParams(0, WRAP_CONTENT).apply {
            topToTop = PARENT_ID
            startToEnd = R.id.user_item_avatar
            bottomToBottom = PARENT_ID
            endToStart = R.id.user_item_notes_indicator
            updateMarginsRelative(start = dp(12))
        }
    }
}

fun createItemNotesIndicatorImageView(context: Context): ImageView {
    return ImageView(context).apply {
        id = R.id.user_item_notes_indicator
        setImageResource(R.drawable.ic_note)
        layoutParams = ConstraintLayout.LayoutParams(dp(32), WRAP_CONTENT).apply {
            topToTop = PARENT_ID
            bottomToBottom = PARENT_ID
            endToEnd = PARENT_ID
            updateMarginsRelative(
                top = dp(12),
                end = dp(16),
                start = dp(8)
            )
        }
    }
}
