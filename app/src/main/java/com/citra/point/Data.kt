package com.citra.point

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ProductCategory(val id: String, val name: String)
data class Product(
    val id: Int, val categoryId: String, val name: String,
    val price: String, val packaging: String, val desc: String
) {
    val categoryName get() = Sample.categories.first { it.id == categoryId }.name
}

/** Data contoh. Ganti dengan Room / Retrofit saat backend siap. */
object Sample {
    val categories = listOf(
        ProductCategory("selempang", "Selempang"), ProductCategory("plakat", "Plakat"),
        ProductCategory("souvenir", "Souvenir"), ProductCategory("pin", "Pin"),
        ProductCategory("samir", "Samir & Medali")
    )
    val products = List(12) { i ->
        val c = categories[i % categories.size]
        Product(
            i + 1, c.id, "${c.name} Akrilik ${i + 1}", "Rp50.000 - Rp75.000", "Box Mika",
            "Produk custom dengan ukiran nama & logo instansi, cocok untuk penghargaan wisuda maupun kenang-kenangan."
        )
    }
}

class AppViewModel : ViewModel() {
    var isLoading by mutableStateOf(true); private set
    var isOffline by mutableStateOf(false); private set   // set true saat gagal memuat dari API
    var darkTheme by mutableStateOf(false)
    var english by mutableStateOf(false)
    var query by mutableStateOf("")
    var chip by mutableStateOf<String?>(null)
    val favorites = mutableStateListOf<Int>()
    val history = mutableStateListOf("piagam")

    init { load() }

    fun load() = viewModelScope.launch {
        isLoading = true; isOffline = false
        delay(1500)                 // simulasi request jaringan
        isLoading = false
    }

    fun toggleFav(id: Int) { if (!favorites.remove(id)) favorites.add(id) }

    fun addHistory(q: String) {
        if (q.isBlank()) return
        history.remove(q); history.add(0, q)
        while (history.size > 5) history.removeLast()
    }

    fun filtered() = Sample.products.filter { p ->
        (chip == null || p.categoryId == chip) &&
            (query.isBlank() || p.name.contains(query, true) || p.categoryName.contains(query, true))
    }
}
