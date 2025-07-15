package kr.djspi.pipe01.util

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout

fun DrawerLayout.applySystemBarInsets(
    headerView: View,
    appbarLayout: View,
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        // drawerlayout bottom margin
        view.updateLayoutParams<MarginLayoutParams> {
            bottomMargin = systemBars.bottom
        }
        // headerview top padding -> fill
        headerView.updatePadding(top = systemBars.bottom, bottom = systemBars.top)
        // appbarlayout top padding -> fill
        appbarLayout.updatePadding(top = systemBars.top)

        WindowInsetsCompat.CONSUMED
    }
}