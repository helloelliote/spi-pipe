package kr.djspi.pipe01

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.room.Room
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.navigation_header.*
import kr.djspi.pipe01.nfc.NfcUtil
import kr.djspi.pipe01.sql.SuperviseDatabase
import kr.djspi.pipe01.util.screenScale
import kr.djspi.pipe01.util.settingsMenuEnabled
import org.jetbrains.anko.toast

open class BaseActivity : AppCompatActivity(), OnNavigationItemSelectedListener {

    lateinit var nfcUtil: NfcUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcUtil = NfcUtil(this, javaClass)
        Thread(Runnable {
            superviseDb = Room.databaseBuilder(
                this@BaseActivity,
                SuperviseDatabase::class.java,
                "db_supervise"
            ).build()
        }).start()
        defPackage = packageName
        screenRatio = screenScale()
    }

    @SuppressLint("InflateParams")
    override fun setContentView(layoutResID: Int) {
        val view = layoutInflater.inflate(R.layout.activity_base, null)
        layoutInflater.inflate(layoutResID, activity_content, true)
        super.setContentView(view)

        setSupportActionBar(toolbar)

        Thread(Runnable {
            ActionBarDrawerToggle(
                this@BaseActivity,
                drawer,
                toolbar,
                R.string.nav_drawer_open,
                R.string.nav_drawer_close
            ).apply {
                isDrawerIndicatorEnabled = false
                setHomeAsUpIndicator(R.drawable.ic_drawer)
                setToolbarNavigationClickListener {
                    drawer.openDrawer(GravityCompat.START)
                }
                drawer.addDrawerListener(this)
                syncState()
            }
            val menu = navView.menu
            navView.setNavigationItemSelectedListener(this@BaseActivity)
            versionName.text = getString(
                R.string.nav_version_name,
                BuildConfig.VERSION_NAME,
                BuildConfig.BUILD_TYPE
            )
            email.text = getString(R.string.nav_email, "djgis@chol.com")
            email.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto", "djgis@chol.com", null)
                    )
                )
            }
            phone.text = getString(R.string.nav_dj_phone)
            phone.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:${phone.text}")
                    )
                )
            }
            val what: Any = TextAppearanceSpan(this@BaseActivity, R.style.TextAppearance20sp)
            val titleApp = menu.findItem(R.id.title_app)
            val spannable = SpannableString(titleApp.title)
            spannable.setSpan(what, 0, spannable.length, 0)
            titleApp.title = spannable

            menu.findItem(R.id.title_setting).isVisible = settingsMenuEnabled()

            nav_close.setOnClickListener { drawer.closeDrawer(GravityCompat.START) }
        })
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        toolbar?.setTitleTextAppearance(this, R.style.TitleHeader)
        nmap_find.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                when {
                    currentLocation != null -> startActivity(
                        Intent(
                            context,
                            NaverMapActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                    else -> toast(getString(R.string.toast_error_location))
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer.closeDrawer(GravityCompat.START, false)

        when (item.itemId) {
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
            else -> {
            }
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPause() {
        super.onPause()
        nfcUtil.onPause()
    }

    companion object {
        lateinit var defPackage: String
        var superviseDb: SuperviseDatabase? = null
        var currentLocation: Location? = null
        var screenRatio: Float = 0.0f
    }
}