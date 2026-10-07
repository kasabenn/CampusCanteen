package com.campus.canteen

/**
 * Model data pesanan kantin kampus.
 */
data class CanteenOrder(
    val orderId: String,
    val menuName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val paymentMethod: String,
    val status: String
)

/**
 * Model item menu makanan/minuman kantin kampus.
 */
data class MenuItem(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val description: String
)
