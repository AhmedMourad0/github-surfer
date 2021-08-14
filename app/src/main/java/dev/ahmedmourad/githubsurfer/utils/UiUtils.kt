package dev.ahmedmourad.githubsurfer.utils

import android.content.res.Resources
import android.graphics.ColorMatrixColorFilter
import android.util.TypedValue
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

fun dp(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}

private val NEGATIVE = floatArrayOf(
    -1.0f,         0f,         0f,         0f,      255f,    // red
    0f,      -1.0f,         0f,         0f,      255f,    // green
    0f,         0f,      -1.0f,         0f,      255f,    // blue
    0f,         0f,         0f,       1.0f,        0f     // alpha
)

private val filter = ColorMatrixColorFilter(NEGATIVE)

fun ImageView.useNegativeColors(use: Boolean) {
    this.colorFilter = if (use) filter else null
}

fun Fragment.hideIme() {
    ViewCompat.getWindowInsetsController(requireView())
        ?.hide(WindowInsetsCompat.Type.ime())
}
