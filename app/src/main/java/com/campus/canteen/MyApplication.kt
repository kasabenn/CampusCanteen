package com.campus.canteen

import android.app.Application
import timber.log.Timber

/**
 * Application class untuk CampusCanteen.
 * Menginisialisasi pustaka logging Timber hanya ketika dalam mode DEBUG
 * agar jejak audit debug steril dari rilis produksi.
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Pasang DebugTree HANYA saat aplikasi dijalankan dalam mode DEBUG
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.i("CampusCanteen Application diinisialisasi dalam mode DEBUG. Timber DebugTree aktif.")
        }
    }
}
