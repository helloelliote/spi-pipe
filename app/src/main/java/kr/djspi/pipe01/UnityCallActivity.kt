package kr.djspi.pipe01

import android.content.Intent
import android.os.Bundle

class UnityCallActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unitycall)
//        button_unity.setOnClickListener {
//            onBackPressed()
//        }
        val intent = Intent(this, kr.djspi.unitysample.UnityPlayerActivity::class.java)
        intent.putExtra("arguments", 50)
        startActivity(intent)
    }
}
