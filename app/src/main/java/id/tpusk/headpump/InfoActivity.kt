package id.tpusk.headpump

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.tpusk.headpump.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {

    private val binding by lazy { ActivityInfoBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }
}