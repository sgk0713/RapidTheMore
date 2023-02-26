package kr.evangers.rapidthemore.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.evangers.rapidthemore.R
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this)
    }
}