package kr.djspi.pipe01.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kr.djspi.pipe01.BuildConfig
import kr.djspi.pipe01.R
import kr.djspi.pipe01.SettingsActivity
import kr.djspi.pipe01.databinding.NavigationHeaderBinding

fun Activity.setNavigationDrawer(
    toolbar: androidx.appcompat.widget.Toolbar,
    navigationView: NavigationView,
    drawerLayout: DrawerLayout
) {
    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    setNavHeader(navigationView)
    setNavItemClickListener(navigationView, drawerLayout)
    setNavIconClickListener(toolbar, drawerLayout)
}

private fun Activity.setNavHeader(navigationView: NavigationView) {
//    val headerView = navigationView.getHeaderView(0)
    val headerBinding = NavigationHeaderBinding.bind(navigationView.getHeaderView(0))
    headerBinding.versionName.text = getString(
        R.string.nav_version_name,
        BuildConfig.VERSION_NAME,
        if (BuildConfig.BUILD_TYPE == "debug") "(DEBUG)" else ""
    )
    headerBinding.contactEmail.text = getString(R.string.nav_email, getString(R.string.nav_dj_email))
    headerBinding.contactPhone.text = getString(R.string.nav_dj_phone)

    val menu = navigationView.menu
    val what: Any = TextAppearanceSpan(this, R.style.TextAppearance20sp)

    val titleApp = menu.findItem(R.id.nav_apptitle)
    val spannable = SpannableString(titleApp.title)
    spannable.setSpan(what, 0, spannable.length, 0)
    titleApp.title = spannable

    val titleSetting = menu.findItem(R.id.nav_settingtitle)
    val spannable2 = SpannableString(titleSetting.title)
    spannable2.setSpan(what, 0, spannable2.length, 0)
    titleSetting.title = spannable2

    menu.findItem(R.id.nav_settingtitle).isVisible = settingsMenuEnabled()
}

private fun Activity.setNavItemClickListener(navigationView: NavigationView, drawerLayout: DrawerLayout) {
    navigationView.setNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.nav_guide -> {
                AlertDialog.Builder(this)
                    .setTitle("SPI 정보")
                    .setMessage(getString(R.string.nav_guide_content))
                    .setPositiveButton("닫기") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .also {
                        it.findViewById<TextView>(android.R.id.message)?.textSize = 14.0f
                        it.show()
                    }
            }

            R.id.nav_manual_video -> {
                val appIntent = Intent(
                    Intent.ACTION_VIEW, Uri.parse("vnd.youtube:6Ttio_ff3n8")
                )
                val webIntent = Intent(
                    Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=6Ttio_ff3n8")
                )
                try {
                    startActivity(appIntent)
                } catch (e: ActivityNotFoundException) {
                    startActivity(webIntent)
                }
            }

            R.id.nav_homepage -> startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.nav_dj_homepage))
                )
            )

            R.id.nav_settings -> startActivity(
                Intent(this, SettingsActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
        drawerLayout.close()
        true
    }
}

private fun setNavIconClickListener(toolbar: androidx.appcompat.widget.Toolbar, drawerLayout: DrawerLayout) {
    toolbar.setNavigationOnClickListener {
        if (drawerLayout.isOpen) drawerLayout.close() else drawerLayout.open()
    }
}