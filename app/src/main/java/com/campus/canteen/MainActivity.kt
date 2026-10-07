package com.campus.canteen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

/**
 * Halaman selamat datang / dashboard utama CampusCanteen.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.d("MainActivity dibuka: Menampilkan dashboard selamat datang CampusCanteen")

        val btnStartOrder = findViewById<Button>(R.id.btnStartOrder)
        btnStartOrder.setOnClickListener {
            Timber.i("Tombol 'Mulai Pesan' ditekan -> Navigasi ke CanteenActivity")
            val intent = Intent(this, CanteenActivity::class.java)
            startActivity(intent)
        }
    }
}
