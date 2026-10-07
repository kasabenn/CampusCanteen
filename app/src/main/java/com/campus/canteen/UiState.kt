package com.campus.canteen

/**
 * Arsitektur status UI berbasis Sealed Class untuk merepresentasikan
 * seluruh siklus hidup layar transaksi pembayaran kantin.
 */
sealed class UiState<out T> {

    /**
     * Kondisi default saat layar siap menerima input pengguna.
     */
    object Idle : UiState<Nothing>()

    /**
     * Kondisi saat transaksi sedang dalam proses verifikasi latar belakang.
     */
    object Loading : UiState<Nothing>()

    /**
     * Kondisi saat transaksi pembayaran berhasil diselesaikan.
     */
    data class Success<out T>(val data: T) : UiState<T>()

    /**
     * Kondisi saat terjadi kesalahan/galat dengan metadata pemulihan.
     * @param message Pesan ramah pengguna yang dapat dibaca di layar
     * @param canRetry Menunjukkan apakah UI menyediakan aksi 'Coba Lagi'
     * @param errorType Kategori error (contoh: TIMEOUT, UNAVAILABLE, HIGH_VALUE, UNKNOWN)
     */
    data class Error(
        val message: String,
        val canRetry: Boolean,
        val errorType: String
    ) : UiState<Nothing>()
}
