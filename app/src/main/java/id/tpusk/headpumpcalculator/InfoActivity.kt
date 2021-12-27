package id.tpusk.headpumpcalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.tpusk.headpumpcalculator.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {

    private val binding by lazy { ActivityInfoBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }
}