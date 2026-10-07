package com.campus.canteen

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID

/**
 * Aktivitas Pemesanan Kantin Kampus (CampusCanteen).
 * Mengimplementasikan Error Handling bertingkat, runCatching, CoroutineExceptionHandler,
 * Retry Pattern, Fallback Mode, dan Debugger-friendly procedures.
 */
class CanteenActivity : AppCompatActivity() {

    // View References - Form & Menu
    private lateinit var tvAppTitle: TextView
    private lateinit var tvAppSubtitle: TextView

    private lateinit var cardMenuChicken: MaterialCardView
    private lateinit var cardMenuNoodle: MaterialCardView
    private lateinit var cardMenuRiceBowl: MaterialCardView
    private lateinit var cardMenuCoffee: MaterialCardView
    private lateinit var cardMenuToast: MaterialCardView

    private lateinit var tvMenuName: TextView
    private lateinit var tvMenuPrice: TextView
    private lateinit var tvBadgeSelectedChicken: TextView
    private lateinit var tvBadgeSelectedNoodle: TextView
    private lateinit var tvBadgeSelectedRiceBowl: TextView
    private lateinit var tvBadgeSelectedCoffee: TextView
    private lateinit var tvBadgeSelectedToast: TextView

    // Stepper & Quantity Input
    private lateinit var tilQuantity: TextInputLayout
    private lateinit var etQuantity: TextInputEditText
    private lateinit var btnQtyMinus: MaterialButton
    private lateinit var btnQtyPlus: MaterialButton
    private lateinit var btnShortcutHighValue: MaterialButton

    // Order Summary
    private lateinit var tvOrderSummary: TextView
    private lateinit var tvQuantity: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvPaymentMethod: TextView

    // Payment Methods
    private lateinit var rgPaymentMethod: RadioGroup
    private lateinit var rbQris: RadioButton
    private lateinit var rbEwallet: RadioButton
    private lateinit var rbCash: RadioButton

    // Simulation Panel
    private lateinit var rgSimulationMode: RadioGroup
    private lateinit var rbSimNormal: RadioButton
    private lateinit var rbSimTimeout: RadioButton
    private lateinit var rbSimMenuUnavailable: RadioButton
    private lateinit var rbSimUnknownError: RadioButton

    // Action & Result Views
    private lateinit var btnPayNow: MaterialButton
    private lateinit var progressPayment: ProgressBar
    private lateinit var cardPaymentResult: CardView
    private lateinit var tvPaymentStatus: TextView
    private lateinit var tvPaymentMessage: TextView
    private lateinit var tvOrderId: TextView
    private lateinit var tvTotalPayment: TextView
    private lateinit var tvRetryInfo: TextView
    private lateinit var btnRetry: MaterialButton
    private lateinit var btnConfirmCash: MaterialButton
    private lateinit var btnOrderAgain: MaterialButton

    // Simulator Engine
    private val paymentSimulator = PaymentSimulator()

    // State Variables
    private var selectedMenuName: String = "Nasi Ayam Sambal Matah"
    private var selectedUnitPrice: Double = 25000.0
    private var currentQuantity: Int = 2
    private var currentTotalPrice: Double = 50000.0

    // Retry Counter (Tugas Mandiri: Max Retry Threshold)
    private var retryAttempt: Int = 0
    private val maxRetryThreshold: Int = 3

    // Supervisor Authorization State
    private var isSupervisorAuthorized: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canteen)

        Timber.i("CanteenActivity diinisialisasi. Menyiapkan elemen UI...")

        initViews()
        setupMenuSelection()
        setupQuantityInputs()
        setupPaymentMethodSelection()
        setupActionButtons()

        // Perbarui ringkasan awal
        updateOrderSummary()
    }

    /**
     * Inisialisasi seluruh komponen View dari layout XML
     */
    private fun initViews() {
        tvAppTitle = findViewById(R.id.tvAppTitle)
        tvAppSubtitle = findViewById(R.id.tvAppSubtitle)

        cardMenuChicken = findViewById(R.id.cardMenuChicken)
        cardMenuNoodle = findViewById(R.id.cardMenuNoodle)
        cardMenuRiceBowl = findViewById(R.id.cardMenuRiceBowl)
        cardMenuCoffee = findViewById(R.id.cardMenuCoffee)
        cardMenuToast = findViewById(R.id.cardMenuToast)

        tvMenuName = findViewById(R.id.tvMenuName)
        tvMenuPrice = findViewById(R.id.tvMenuPrice)
        tvBadgeSelectedChicken = findViewById(R.id.tvBadgeSelectedChicken)
        tvBadgeSelectedNoodle = findViewById(R.id.tvBadgeSelectedNoodle)
        tvBadgeSelectedRiceBowl = findViewById(R.id.tvBadgeSelectedRiceBowl)
        tvBadgeSelectedCoffee = findViewById(R.id.tvBadgeSelectedCoffee)
        tvBadgeSelectedToast = findViewById(R.id.tvBadgeSelectedToast)

        tilQuantity = findViewById(R.id.tilQuantity)
        etQuantity = findViewById(R.id.etQuantity)
        btnQtyMinus = findViewById(R.id.btnQtyMinus)
        btnQtyPlus = findViewById(R.id.btnQtyPlus)
        btnShortcutHighValue = findViewById(R.id.btnShortcutHighValue)

        tvOrderSummary = findViewById(R.id.tvOrderSummary)
        tvQuantity = findViewById(R.id.tvQuantity)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod)

        rgPaymentMethod = findViewById(R.id.rgPaymentMethod)
        rbQris = findViewById(R.id.rbQris)
        rbEwallet = findViewById(R.id.rbEwallet)
        rbCash = findViewById(R.id.rbCash)

        rgSimulationMode = findViewById(R.id.rgSimulationMode)
        rbSimNormal = findViewById(R.id.rbSimNormal)
        rbSimTimeout = findViewById(R.id.rbSimTimeout)
        rbSimMenuUnavailable = findViewById(R.id.rbSimMenuUnavailable)
        rbSimUnknownError = findViewById(R.id.rbSimUnknownError)

        btnPayNow = findViewById(R.id.btnPayNow)
        progressPayment = findViewById(R.id.progressPayment)
        cardPaymentResult = findViewById(R.id.cardPaymentResult)
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus)
        tvPaymentMessage = findViewById(R.id.tvPaymentMessage)
        tvOrderId = findViewById(R.id.tvOrderId)
        tvTotalPayment = findViewById(R.id.tvTotalPayment)
        tvRetryInfo = findViewById(R.id.tvRetryInfo)
        btnRetry = findViewById(R.id.btnRetry)
        btnConfirmCash = findViewById(R.id.btnConfirmCash)
        btnOrderAgain = findViewById(R.id.btnOrderAgain)
    }

    /**
     * Konfigurasi pilihan menu makanan
     */
    private fun setupMenuSelection() {
        cardMenuChicken.setOnClickListener {
            selectMenuItem("Nasi Ayam Sambal Matah", 25000.0, 1)
        }
        cardMenuNoodle.setOnClickListener {
            selectMenuItem("Mie Ayam Bakso", 18000.0, 2)
        }
        cardMenuRiceBowl.setOnClickListener {
            selectMenuItem("Rice Bowl Chicken", 22000.0, 3)
        }
        cardMenuCoffee.setOnClickListener {
            selectMenuItem("Es Kopi Susu", 12000.0, 4)
        }
        cardMenuToast.setOnClickListener {
            selectMenuItem("Roti Bakar Coklat", 15000.0, 5)
        }
    }

    private fun selectMenuItem(name: String, price: Double, itemIndex: Int) {
        selectedMenuName = name
        selectedUnitPrice = price
        isSupervisorAuthorized = false // Reset otorisasi jika menu diganti
        Timber.d("User memilih menu: %s (Harga: Rp %,.0f)", name, price)

        // Reset visual card strokes & badges
        val primaryColor = getColor(R.color.primary)
        val strokeColor = getColor(R.color.stroke_color)

        cardMenuChicken.strokeColor = if (itemIndex == 1) primaryColor else strokeColor
        cardMenuChicken.strokeWidth = if (itemIndex == 1) 4 else 2
        tvBadgeSelectedChicken.text = if (itemIndex == 1) "Dipilih" else "Pilih"

        cardMenuNoodle.strokeColor = if (itemIndex == 2) primaryColor else strokeColor
        cardMenuNoodle.strokeWidth = if (itemIndex == 2) 4 else 2
        tvBadgeSelectedNoodle.text = if (itemIndex == 2) "Dipilih" else "Pilih"

        cardMenuRiceBowl.strokeColor = if (itemIndex == 3) primaryColor else strokeColor
        cardMenuRiceBowl.strokeWidth = if (itemIndex == 3) 4 else 2
        tvBadgeSelectedRiceBowl.text = if (itemIndex == 3) "Dipilih" else "Pilih"

        cardMenuCoffee.strokeColor = if (itemIndex == 4) primaryColor else strokeColor
        cardMenuCoffee.strokeWidth = if (itemIndex == 4) 4 else 2
        tvBadgeSelectedCoffee.text = if (itemIndex == 4) "Dipilih" else "Pilih"

        cardMenuToast.strokeColor = if (itemIndex == 5) primaryColor else strokeColor
        cardMenuToast.strokeWidth = if (itemIndex == 5) 4 else 2
        tvBadgeSelectedToast.text = if (itemIndex == 5) "Dipilih" else "Pilih"

        updateOrderSummary()
    }

    /**
     * Konfigurasi input kuantitas dan stepper buttons
     */
    private fun setupQuantityInputs() {
        btnQtyMinus.setOnClickListener {
            val qty = etQuantity.text.toString().toIntOrNull() ?: 1
            if (qty > 1) {
                etQuantity.setText((qty - 1).toString())
            }
        }

        btnQtyPlus.setOnClickListener {
            val qty = etQuantity.text.toString().toIntOrNull() ?: 1
            etQuantity.setText((qty + 1).toString())
        }

        // Shortcut tombol untuk mempermudah pengujian transaksi besar (> Rp 10.000.000)
        btnShortcutHighValue.setOnClickListener {
            // Set kuantitas menjadi 450 porsi sehingga total Rp 11.250.000 (> 10 Juta)
            etQuantity.setText("450")
            Toast.makeText(this, "Kuantitas diatur ke 450 porsi untuk menguji batas Rp 10.000.000", Toast.LENGTH_SHORT).show()
        }

        etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val inputStr = s?.toString()?.trim() ?: ""
                Timber.d("Kuantitas diubah oleh pengguna: %s", inputStr)

                // Defensive validation parsing
                val qty = inputStr.toIntOrNull()
                if (qty == null || qty < 1) {
                    tilQuantity.error = "Jumlah pesanan minimal 1."
                } else {
                    tilQuantity.error = null
                    currentQuantity = qty
                    isSupervisorAuthorized = false // Reset otorisasi jika jumlah berubah
                    updateOrderSummary()
                }
            }
        })
    }

    /**
     * Konfigurasi pemilihan metode pembayaran
     */
    private fun setupPaymentMethodSelection() {
        rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            val method = when (checkedId) {
                R.id.rbEwallet -> "E-Wallet"
                R.id.rbCash -> "Tunai Manual"
                else -> "QRIS"
            }
            tvPaymentMethod.text = method
            Timber.d("User memilih metode pembayaran: %s", method)
        }
    }

    /**
     * Konfigurasi tombol aksi
     */
    private fun setupActionButtons() {
        // Tombol Bayar Sekarang
        btnPayNow.setOnClickListener {
            Timber.i("Tombol 'Bayar Sekarang' ditekan")
            processOrder()
        }

        // Tombol Coba Lagi (Retry)
        btnRetry.setOnClickListener {
            Timber.i("Tombol 'Coba Lagi' ditekan (Percobaan ke-%d)", retryAttempt + 1)
            executePayment(isRetry = true)
        }

        // Tombol Konfirmasi Pembayaran Tunai (Recovery Fallback)
        btnConfirmCash.setOnClickListener {
            confirmManualCashPayment()
        }

        // Tombol Pesan Lagi (Reset)
        btnOrderAgain.setOnClickListener {
            resetOrderState()
        }
    }

    /**
     * Menghitung subtotal pesanan secara eksplisit.
     * Kode dibuat bertingkat dan ramah debugger untuk evaluasi variabel & breakpoints.
     */
    fun calculateOrderTotal(unitPrice: Double, quantity: Int): Double {
        // BREAKPOINT DEMO:
        // Pasang breakpoint di sini untuk melihat quantity dan subtotal.
        val localQty = quantity
        val localPrice = unitPrice
        val subtotal = localPrice * localQty
        return subtotal
    }

    /**
     * Memperbarui informasi ringkasan pesanan di layar
     */
    private fun updateOrderSummary() {
        currentTotalPrice = calculateOrderTotal(selectedUnitPrice, currentQuantity)

        tvOrderSummary.text = selectedMenuName
        tvQuantity.text = "$currentQuantity porsi"
        tvSubtotal.text = "Rp %,.0f".format(currentTotalPrice)
    }

    /**
     * Mengambil nama metode pembayaran yang aktif
     */
    private fun getSelectedPaymentMethod(): String {
        return when (rgPaymentMethod.checkedRadioButtonId) {
            R.id.rbEwallet -> "E-Wallet"
            R.id.rbCash -> "Tunai Manual"
            else -> "QRIS"
        }
    }

    /**
     * Mengambil mode simulasi pengujian yang aktif
     */
    private fun getSelectedSimulationMode(): SimulationMode {
        return when (rgSimulationMode.checkedRadioButtonId) {
            R.id.rbSimTimeout -> SimulationMode.PAYMENT_TIMEOUT
            R.id.rbSimMenuUnavailable -> SimulationMode.MENU_UNAVAILABLE
            R.id.rbSimUnknownError -> SimulationMode.UNKNOWN_ERROR
            else -> SimulationMode.NORMAL
        }
    }

    /**
     * Logika utama pemrosesan pesanan sebelum eksekusi pembayaran.
     * Menggunakan variabel lokal yang jelas agar mudah diinspeksi di panel 'Variables' Debugger.
     */
    private fun processOrder() {
        val quantityStr = etQuantity.text.toString().trim()

        // Validasi defensif: input kosong atau tidak valid
        val validQty = quantityStr.toIntOrNull()
        if (validQty == null || validQty < 1) {
            tilQuantity.error = "Jumlah pesanan minimal 1."
            Timber.w("Validasi gagal: Jumlah kuantitas tidak valid (%s)", quantityStr)
            Toast.makeText(this, "Harap masukkan jumlah pesanan yang valid (minimal 1)", Toast.LENGTH_SHORT).show()
            return
        }

        tilQuantity.error = null

        // BREAKPOINT DEMO:
        // Periksa paymentMethod dan totalPrice sebelum pembayaran.
        val quantity = validQty
        val unitPrice = selectedUnitPrice
        val subtotal = calculateOrderTotal(unitPrice, quantity)
        val paymentMethod = getSelectedPaymentMethod()

        Timber.d("Menyiapkan transaksi: Menu=%s, Qty=%d, Subtotal=Rp %,.0f, Metode=%s",
            selectedMenuName, quantity, subtotal, paymentMethod)

        // Validasi transaksi bernilai tinggi (> Rp 10.000.000)
        if (subtotal > 10_000_000.0 && !isSupervisorAuthorized) {
            Timber.w("Transaksi bernilai tinggi terdeteksi: Rp %,.0f (> Rp 10.000.000). Membutuhkan otorisasi supervisor.", subtotal)
            showSupervisorPinDialog(subtotal)
            return
        }

        // Jalankan eksekusi pembayaran normal (bukan retry)
        retryAttempt = 0
        executePayment(isRetry = false)
    }

    /**
     * Eksekusi alur pembayaran menggunakan Coroutines, runCatching, dan CoroutineExceptionHandler
     */
    private fun executePayment(isRetry: Boolean) {
        setUiState(UiState.Loading)

        val paymentMethod = getSelectedPaymentMethod()
        val simMode = getSelectedSimulationMode()
        val order = CanteenOrder(
            orderId = "ORD-CC-2026-" + UUID.randomUUID().toString().take(6).uppercase(),
            menuName = selectedMenuName,
            quantity = currentQuantity,
            unitPrice = selectedUnitPrice,
            totalPrice = currentTotalPrice,
            paymentMethod = paymentMethod,
            status = "PENDING"
        )

        Timber.d("Memulai alur pembayaran untuk OrderID: %s (Mode: %s, Retry: %b)",
            order.orderId, simMode.name, isRetry)

        // Coroutine Exception Handler: Pengaman terakhir agar aplikasi tidak pernah crash / force close
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
            Timber.e(exception, "FATAL COROUTINE ERROR TERTANGKAP: %s", exception.message)
            runOnUiThread {
                setUiState(
                    UiState.Error(
                        message = "Terjadi kesalahan coroutine tidak terduga: ${exception.localizedMessage}",
                        canRetry = false,
                        errorType = "FATAL_COROUTINE"
                    )
                )
            }
        }

        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            // Menggunakan idiom fungsional runCatching untuk menangkal seluruh error perbankan/gateway
            val paymentResult = runCatching {
                paymentSimulator.processPayment(order, simMode)
            }

            withContext(Dispatchers.Main) {
                paymentResult.onSuccess { completedOrder ->
                    Timber.i("Pembayaran sukses tercatat: OrderID=%s, Total=Rp %,.0f",
                        completedOrder.orderId, completedOrder.totalPrice)
                    retryAttempt = 0 // Reset counter retry jika berhasil
                    setUiState(UiState.Success(completedOrder))
                }.onFailure { throwable ->
                    handlePaymentFailure(throwable, order)
                }
            }
        }
    }

    /**
     * Penanganan eksepsi bertingkat berdasarkan jenis Custom Exception
     */
    private fun handlePaymentFailure(throwable: Throwable, order: CanteenOrder) {
        Timber.w("Transaksi pembayaran gagal: %s [Tipe: %s]",
            throwable.message, throwable.javaClass.simpleName)

        when (throwable) {
            is HighValueOrderException -> {
                Timber.w("Menangani HighValueOrderException")
                setUiState(
                    UiState.Error(
                        message = throwable.message ?: "Pesanan membutuhkan otorisasi supervisor.",
                        canRetry = false,
                        errorType = "HIGH_VALUE"
                    )
                )
                showSupervisorPinDialog(order.totalPrice)
            }

            is PaymentTimeoutException -> {
                retryAttempt++
                Timber.w("Payment timeout terjadi. Retry attempt: %d dari %d", retryAttempt, maxRetryThreshold)

                if (retryAttempt >= maxRetryThreshold) {
                    // Batas retry tercapai (3x berturut-turut gagal) -> Tampilkan dialog rekomendasi Tunai
                    showMaxRetryFallbackDialog()
                } else {
                    setUiState(
                        UiState.Error(
                            message = "Pembayaran sedang mengalami gangguan koneksi.\nSilakan coba kembali.",
                            canRetry = true,
                            errorType = "TIMEOUT"
                        )
                    )
                }
            }

            is MenuUnavailableException -> {
                setUiState(
                    UiState.Error(
                        message = "Menu yang dipilih sedang tidak tersedia.\nSilakan pilih menu lain.",
                        canRetry = false,
                        errorType = "MENU_UNAVAILABLE"
                    )
                )
            }

            else -> {
                // Log detail lengkap untuk developer di Timber, jangan tampilkan stack trace ke pengguna
                Timber.e(throwable, "Terjadi kesalahan tidak terduga pada payment engine")
                setUiState(
                    UiState.Error(
                        message = "Terjadi kesalahan yang tidak terduga.\nSilakan coba kembali.",
                        canRetry = true,
                        errorType = "UNKNOWN"
                    )
                )
            }
        }
    }

    /**
     * Mengatur status antarmuka pengguna berdasarkan Sealed Class UiState
     */
    private fun setUiState(state: UiState<CanteenOrder>) {
        when (state) {
            is UiState.Idle -> {
                progressPayment.visibility = View.GONE
                btnPayNow.isEnabled = true
                cardPaymentResult.visibility = View.GONE
                btnRetry.visibility = View.GONE
                btnConfirmCash.visibility = View.GONE
                tvRetryInfo.visibility = View.GONE
            }

            is UiState.Loading -> {
                progressPayment.visibility = View.VISIBLE
                btnPayNow.isEnabled = false
                btnRetry.isEnabled = false
                cardPaymentResult.visibility = View.GONE
            }

            is UiState.Success -> {
                progressPayment.visibility = View.GONE
                btnPayNow.isEnabled = true
                btnRetry.isEnabled = true

                cardPaymentResult.visibility = View.VISIBLE
                tvPaymentStatus.text = "PEMBAYARAN BERHASIL"
                tvPaymentStatus.setTextColor(getColor(R.color.success_text))

                tvPaymentMessage.text = "Pembayaran lunas via ${state.data.paymentMethod}. Pesanan #${state.data.orderId} segera disajikan!"
                tvOrderId.text = state.data.orderId
                tvTotalPayment.text = "Rp %,.0f".format(state.data.totalPrice)

                btnRetry.visibility = View.GONE
                btnConfirmCash.visibility = View.GONE
                tvRetryInfo.visibility = View.GONE
                btnOrderAgain.visibility = View.VISIBLE
            }

            is UiState.Error -> {
                progressPayment.visibility = View.GONE
                btnPayNow.isEnabled = true
                btnRetry.isEnabled = true

                cardPaymentResult.visibility = View.VISIBLE
                tvPaymentStatus.text = "PEMBAYARAN GAGAL"
                tvPaymentStatus.setTextColor(getColor(R.color.error_text))
                tvPaymentMessage.text = state.message

                tvOrderId.text = "-"
                tvTotalPayment.text = "Rp %,.0f".format(currentTotalPrice)

                // Tampilkan info retry jika diperbolehkan
                if (state.canRetry) {
                    btnRetry.visibility = View.VISIBLE
                    tvRetryInfo.visibility = View.VISIBLE
                    tvRetryInfo.text = "Percobaan $retryAttempt dari $maxRetryThreshold"
                } else {
                    btnRetry.visibility = View.GONE
                    tvRetryInfo.visibility = View.GONE
                }

                btnConfirmCash.visibility = View.GONE
                btnOrderAgain.visibility = View.VISIBLE

                // Tampilkan pula Snackbar ramah pengguna
                tampilkanSnackbarError(state.message, state.canRetry)
            }
        }
    }

    /**
     * Menampilkan Snackbar kesalahan dengan tombol Coba Lagi jika diizinkan
     */
    private fun tampilkanSnackbarError(pesan: String, canRetry: Boolean) {
        val root = findViewById<View>(R.id.coordinatorLayout)
        val cleanMessage = pesan.replace("\n", " ")
        val snackbar = Snackbar.make(root, cleanMessage, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#991B1B"))
        snackbar.setTextColor(Color.WHITE)

        if (canRetry) {
            snackbar.setAction("COBA LAGI") {
                Timber.i("Pengguna menekan aksi 'COBA LAGI' pada Snackbar")
                executePayment(isRetry = true)
            }
            snackbar.setActionTextColor(Color.parseColor("#FEF08A"))
        }
        snackbar.show()
    }

    /**
     * Dialog saat retry mencapai batas 3 kali berturut-turut.
     * Mengarahkan pelanggan ke mode pembayaran alternatif (Fallback: Tunai Manual).
     */
    private fun showMaxRetryFallbackDialog() {
        Timber.w("Ambang batas max retry (3x) tercapai. Membuka dialog rekomendasi pembayaran tunai manual.")

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_max_retry_title)
            .setMessage(R.string.dialog_max_retry_message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(false)
            .setPositiveButton(R.string.btn_pay_cash) { dialog, _ ->
                dialog.dismiss()
                Timber.i("Pengguna memilih beralih ke Fallback Mode: Tunai Manual")
                switchToCashManualFallback()
            }
            .setNegativeButton(R.string.btn_close) { dialog, _ ->
                dialog.dismiss()
                Timber.d("Pengguna menutup dialog max retry")
                setUiState(
                    UiState.Error(
                        message = "Koneksi pembayaran gagal setelah 3x percobaan. Silakan coba kembali nanti.",
                        canRetry = false,
                        errorType = "MAX_RETRY_EXCEEDED"
                    )
                )
            }
            .show()
    }

    /**
     * Mengalihkan transaksi ke metode tunai manual (Fallback Payment Mode)
     */
    private fun switchToCashManualFallback() {
        // Ganti RadioButton ke Tunai Manual
        rbCash.isChecked = true
        tvPaymentMethod.text = "Cash Manual"
        retryAttempt = 0

        // Perbarui tampilan status hasil
        cardPaymentResult.visibility = View.VISIBLE
        tvPaymentStatus.text = "MENUNGGU PEMBAYARAN"
        tvPaymentStatus.setTextColor(getColor(R.color.warning_text))

        tvPaymentMessage.text = "Pembayaran dialihkan ke metode tunai manual. Silakan tunjukkan pesanan ini ke loket kasir kantin untuk pelunasan."
        tvOrderId.text = "ORD-CC-CASH-" + UUID.randomUUID().toString().take(6).uppercase()
        tvTotalPayment.text = "Rp %,.0f".format(currentTotalPrice)

        // Sembunyikan retry, munculkan tombol konfirmasi tunai
        btnRetry.visibility = View.GONE
        tvRetryInfo.visibility = View.GONE
        btnConfirmCash.visibility = View.VISIBLE
        btnOrderAgain.visibility = View.VISIBLE

        Toast.makeText(this, "Metode dialihkan ke Tunai Manual", Toast.LENGTH_SHORT).show()
        Timber.i("Fallback mode diaktifkan: Status = MENUNGGU PEMBAYARAN")
    }

    /**
     * Konfirmasi pelunasan pembayaran tunai oleh kasir loket
     */
    private fun confirmManualCashPayment() {
        Timber.i("Kasir loket mengonfirmasi pembayaran tunai manual untuk total Rp %,.0f", currentTotalPrice)

        tvPaymentStatus.text = "PEMBAYARAN BERHASIL"
        tvPaymentStatus.setTextColor(getColor(R.color.success_text))

        tvPaymentMessage.text = "Pembayaran tunai manual telah lunas diterima oleh kasir loket. Pesanan sedang diproses di dapur kantin!"
        btnConfirmCash.visibility = View.GONE

        Toast.makeText(this, "Pembayaran tunai berhasil dikonfirmasi!", Toast.LENGTH_LONG).show()
    }

    /**
     * Dialog Custom untuk Otorisasi Transaksi Bernilai Besar (> Rp 10.000.000)
     */
    private fun showSupervisorPinDialog(totalAmount: Double) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_supervisor_pin, null)
        val etPin = dialogView.findViewById<EditText>(R.id.etSupervisorPin)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelPin)
        val btnAuthorize = dialogView.findViewById<Button>(R.id.btnAuthorizePin)
        val tilPin = dialogView.findViewById<TextInputLayout>(R.id.tilSupervisorPin)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
            Timber.d("Otorisasi supervisor dibatalkan oleh pengguna")
            setUiState(
                UiState.Error(
                    message = "Transaksi bernilai besar dibatalkan karena tidak ada otorisasi supervisor.",
                    canRetry = false,
                    errorType = "AUTH_CANCELLED"
                )
            )
        }

        btnAuthorize.setOnClickListener {
            val enteredPin = etPin.text.toString().trim()

            // Verifikasi PIN supervisor (DEMO: 123456)
            // JANGAN pernah mencatat PIN supervisor ke dalam Logcat/Timber!
            if (enteredPin == "123456") {
                Timber.i("Otorisasi supervisor BERHASIL untuk transaksi Rp %,.0f", totalAmount)
                isSupervisorAuthorized = true
                dialog.dismiss()

                Toast.makeText(this, "Otorisasi Supervisor Berhasil! Melanjutkan transaksi...", Toast.LENGTH_SHORT).show()

                // Lanjutkan transaksi secara otomatis
                retryAttempt = 0
                executePayment(isRetry = false)
            } else {
                // Catat kegagalan tanpa mencatat isi PIN
                Timber.w("Percobaan otorisasi supervisor GAGAL: PIN tidak sesuai")
                tilPin.error = "PIN Supervisor salah."
                Toast.makeText(this, "PIN Supervisor salah. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    /**
     * Mereset seluruh kondisi pemesanan ke keadaan awal
     */
    private fun resetOrderState() {
        Timber.i("Mereset formulir dan status pemesanan kantin...")
        retryAttempt = 0
        isSupervisorAuthorized = false
        rbSimNormal.isChecked = true
        rbQris.isChecked = true
        etQuantity.setText("2")
        selectMenuItem("Nasi Ayam Sambal Matah", 25000.0, 1)
        setUiState(UiState.Idle)
        Toast.makeText(this, "Silakan buat pesanan baru", Toast.LENGTH_SHORT).show()
    }
}
