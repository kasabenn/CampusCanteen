package com.campus.canteen

import java.io.IOException

/**
 * 1. Eksepsi saat menu makanan yang dipilih tidak tersedia/habis di dapur kantin.
 */
class MenuUnavailableException(
    message: String = "Menu sedang tidak tersedia. Silakan pilih menu lain."
) : Exception(message)

/**
 * 2. Eksepsi saat layanan payment gateway mengalami gangguan operasional.
 */
class PaymentGatewayException(
    val gatewayName: String,
    message: String = "Layanan pembayaran gateway $gatewayName sedang mengalami gangguan operasional."
) : IOException(message)

/**
 * 3. Eksepsi saat koneksi ke payment gateway mengalami timeout.
 */
class PaymentTimeoutException(
    val gatewayName: String,
    message: String = "Koneksi ke gateway $gatewayName melebihi batas waktu (timeout)."
) : IOException(message)

/**
 * 4. Eksepsi saat total transaksi bernilai besar (> Rp 10.000.000) dan membutuhkan otorisasi supervisor.
 */
class HighValueOrderException(
    val totalAmount: Double,
    message: String = "Pesanan melebihi batas transaksi dan membutuhkan otorisasi supervisor."
) : Exception(message)
