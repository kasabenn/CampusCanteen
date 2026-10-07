package com.campus.canteen

import kotlinx.coroutines.delay

/**
 * Mode pengujian simulasi transaksi untuk demonstrasi praktikum error handling.
 */
enum class SimulationMode {
    NORMAL,
    PAYMENT_TIMEOUT,
    MENU_UNAVAILABLE,
    UNKNOWN_ERROR
}

/**
 * Komponen simulator pembayaran lokal tanpa memerlukan koneksi perbankan/gateway eksternal.
 * Menggunakan Kotlin Coroutines suspend function dan delay() non-blocking.
 */
class PaymentSimulator {

    /**
     * Memproses simulasi pembayaran pesanan kantin kampus.
     *
     * @param order Data pesanan yang akan diproses
     * @param simulationMode Mode simulasi galat yang dipilih
     * @return CanteenOrder dengan status terbarukan jika sukses
     * @throws HighValueOrderException Jika total nominal > Rp 10.000.000
     * @throws MenuUnavailableException Jika mode simulasi MENU_UNAVAILABLE
     * @throws PaymentTimeoutException Jika mode simulasi PAYMENT_TIMEOUT
     * @throws IllegalStateException Jika mode simulasi UNKNOWN_ERROR
     */
    suspend fun processPayment(
        order: CanteenOrder,
        simulationMode: SimulationMode
    ): CanteenOrder {
        // Simulasi latensi jaringan pembayaran (1200ms) menggunakan delay non-blocking
        delay(1200)

        // Validasi transaksi bernilai tinggi (> Rp 10.000.000)
        if (order.totalPrice > 10_000_000.0) {
            throw HighValueOrderException(
                totalAmount = order.totalPrice,
                message = "Pesanan melebihi batas transaksi (Rp %,.0f) dan membutuhkan otorisasi supervisor.".format(order.totalPrice)
            )
        }

        // Simulasi berdasarkan mode pengujian
        return when (simulationMode) {
            SimulationMode.MENU_UNAVAILABLE -> {
                throw MenuUnavailableException("Menu [${order.menuName}] sedang tidak tersedia. Silakan pilih menu lain.")
            }
            SimulationMode.PAYMENT_TIMEOUT -> {
                throw PaymentTimeoutException(
                    gatewayName = order.paymentMethod,
                    message = "Layanan pembayaran gateway ${order.paymentMethod} tidak merespons dalam 15 detik (Timeout)."
                )
            }
            SimulationMode.UNKNOWN_ERROR -> {
                throw IllegalStateException("Terjadi kesalahan tidak terduga pada payment gateway engine (HTTP 500 Unhandled Exception).")
            }
            SimulationMode.NORMAL -> {
                order.copy(
                    status = "PAID"
                )
            }
        }
    }
}
