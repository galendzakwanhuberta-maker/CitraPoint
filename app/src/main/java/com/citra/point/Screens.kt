package com.citra.point

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

// GANTI dengan data asli kamu
const val WA_URL = "https://wa.me/6281234567890"
const val IG_URL = "https://instagram.com/citraproductions"
const val MAPS_URL = "geo:0,0?q=Citra+Productions"


@Composable
fun HomeScreen(vm: AppViewModel, nav: NavController) {
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (vm.isLoading) {
                Text("Memuat kategori dan produk...", fontSize = 11.sp)
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            if (vm.isOffline) OfflineBanner { vm.load() }
            SectionTitle("Kategori Produk")
            if (vm.isLoading) SkeletonHome() else CategoryGrid(null) { nav.navigate("category/$it") }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                SectionTitle("Produk Unggulan")
                Text("Lihat semua", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { nav.goTab("search") })
            }
            if (vm.isLoading) SkeletonProducts()
            else ProductGrid(Sample.products.take(4), vm.favorites, vm::toggleFav) { nav.navigate("product/${it.id}") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(vm: AppViewModel, nav: NavController) {
    var page by remember(vm.query, vm.chip) { mutableIntStateOf(1) }
    val list = vm.filtered()
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedCard(onClick = { nav.navigate("searchinput") }, Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                    Text(vm.query.ifBlank { "Cari produk atau kategori..." }, fontSize = 14.sp,
                        color = if (vm.query.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(vm.chip == null, { vm.chip = null }, { Text("Semua") })
                Sample.categories.forEach { c ->
                    FilterChip(vm.chip == c.id, { vm.chip = c.id }, { Text(c.name) })
                }
            }
            ProductGrid(pageItems(list, page), vm.favorites, vm::toggleFav) { nav.navigate("product/${it.id}") }
            if (list.isEmpty()) Text("Produk tidak ditemukan.", Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Pagination(page, pageCount(list.size)) { page = it }
        }
    }
}

@Composable
fun SearchInputScreen(vm: AppViewModel, nav: NavController) {
    var text by remember { mutableStateOf(vm.query) }
    fun submit(q: String) { vm.query = q.trim(); vm.addHistory(q.trim()); nav.popBackStack() }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton({ nav.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") }
            OutlinedTextField(
                text, { text = it }, Modifier.weight(1f), singleLine = true,
                placeholder = { Text("Cari produk atau kategori...") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submit(text) })
            )
        }
        Spacer(Modifier.height(12.dp))
        Text("Pencarian terakhir", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            vm.history.toList().forEach { h ->
                Row(Modifier.fillMaxWidth().clickable { submit(h) }.padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, Modifier.size(18.dp)); Spacer(Modifier.width(12.dp))
                    Text(h, Modifier.weight(1f))
                    IconButton({ vm.history.remove(h) }) { Icon(Icons.Default.Close, "Hapus", Modifier.size(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun CategoryScreen(id: String, vm: AppViewModel, nav: NavController) {
    var page by remember(id) { mutableIntStateOf(1) }
    val list = Sample.products.filter { it.categoryId == id }
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        BackRow("Kembali") { nav.popBackStack() }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Kategori Produk")
            CategoryGrid(id) { nav.navigate("category/$it") { popUpTo("home") } }
            ProductGrid(pageItems(list, page), vm.favorites, vm::toggleFav) { nav.navigate("product/${it.id}") }
            Pagination(page, pageCount(list.size)) { page = it }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProductDetailScreen(id: Int, vm: AppViewModel, nav: NavController) {
    val p = Sample.products.first { it.id == id }
    val pager = rememberPagerState(pageCount = { 6 })
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            LogoBar()
            BackRow("Kembali") { nav.popBackStack() }
            Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box {
                    HorizontalPager(pager) { ProductImage(Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(8.dp))) }
                    Text("${pager.currentPage + 1}/6 ${p.categoryName.lowercase()} standar", color = Color.White, fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(6.dp, 2.dp))
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(6) { i ->
                        ProductImage(Modifier.size(40.dp).clip(RoundedCornerShape(4.dp))
                            .border(if (i == pager.currentPage) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                            .clickable { scope.launch { pager.animateScrollToPage(i) } })
                    }
                }
                Text(p.price, fontSize = 22.sp)
                Text(p.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Spesifikasi", fontWeight = FontWeight.Bold)
                SpecRow("kategori:", p.categoryName); HorizontalDivider(); SpecRow("packaging:", p.packaging)
                Text("Deskripsi", fontWeight = FontWeight.Bold)
                Text(p.desc, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(88.dp))
            }
        }
        FloatingActionButton({ vm.toggleFav(p.id) }, Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
            Icon(if (p.id in vm.favorites) Icons.Default.Star else Icons.Default.StarBorder, "Favorit")
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) = Row(Modifier.padding(vertical = 4.dp)) {
    Text(label, Modifier.width(100.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, fontSize = 13.sp)
}

@Composable
fun FavoritesScreen(vm: AppViewModel, nav: NavController) {
    var page by remember { mutableIntStateOf(1) }
    val list = Sample.products.filter { it.id in vm.favorites }
    if (page > pageCount(list.size)) page = pageCount(list.size)
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Produk Favorit")
            Text("Menampilkan ${list.size} dari ${list.size} produk yang di favoritkan", fontSize = 11.sp)
            if (list.isEmpty()) Text("Belum ada favorit. Tekan ikon bintang pada produk.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            ProductGrid(pageItems(list, page), vm.favorites, vm::toggleFav) { nav.navigate("product/${it.id}") }
            Pagination(page, pageCount(list.size)) { page = it }
        }
    }
}

@Composable
private fun SettingCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String? = null,
    onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit = {}
) {
    Card(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                Text(title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                if (onClick != null) Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp))
            }
            if (subtitle != null) Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun Label(t: String) = Text(t, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
fun OthersScreen(vm: AppViewModel, nav: NavController) {
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Label("FAQ")
            SettingCard(Icons.Default.Info, "Pertanyaan umum", "Lihat jawaban cepat untuk pertanyaan yang sering ditanyakan.") { nav.navigate("faq") }
            Label("KONTAK")
            SettingCard(Icons.Default.Email, "Hubungi admin", "Butuh bantuan lebih lanjut? Hubungi tim admin kami.") { nav.navigate("contact") }
            Label("TEMA APLIKASI")
            SettingCard(Icons.Default.WbSunny, "Mode tampilan") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(!vm.darkTheme, { vm.darkTheme = false }, { Text("Terang") }, Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.WbSunny, null, Modifier.size(16.dp)) })
                    FilterChip(vm.darkTheme, { vm.darkTheme = true }, { Text("Gelap") }, Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.DarkMode, null, Modifier.size(16.dp)) })
                }
            }
            Label("BAHASA")
            SettingCard(Icons.Default.Language, "Bahasa aplikasi") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(!vm.english, { vm.english = false }, { Text("Bahasa Indonesia") }, Modifier.weight(1f))
                    FilterChip(vm.english, { vm.english = true }, { Text("English") }, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ContactScreen(nav: NavController) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        BackRow("Kembali ke Others Activity") { nav.popBackStack() }
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedCard({ ctx.openUrl(MAPS_URL) }, Modifier.weight(1f)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, null); Text("Lokasi", fontSize = 13.sp)
                    }
                }
                OutlinedCard({ ctx.openUrl(IG_URL) }, Modifier.weight(1f)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, null); Text("Instagram", fontSize = 13.sp)
                    }
                }
            }
            WhatsAppButton({ ctx.openUrl(WA_URL) })
        }
    }
}

@Composable
fun FaqScreen(nav: NavController) {
    val ctx = LocalContext.current
    val faqs = listOf(
        "Bagaimana cara memesan produk?" to "Pilih produk di katalog, lalu buka halaman detail. Gunakan tombol WhatsApp untuk menanyakan ketersediaan dan mendiskusikan pesanan dengan tim Citra.",
        "Di mana saya bisa melihat harga produk?" to "Rentang harga tampil di kartu produk dan halaman detail.",
        "Bisakah produk memakai desain saya?" to "Bisa. Kirim desain Anda melalui WhatsApp.",
        "Bagaimana cara menanyakan pengiriman?" to "Hubungi admin melalui WhatsApp untuk info ongkos dan estimasi kirim.",
        "Bagaimana cara mengecek status pesanan?" to "Hubungi admin melalui WhatsApp dengan menyebutkan nama pemesan."
    )
    Column(Modifier.fillMaxSize()) {
        LogoBar()
        BackRow("Kembali ke Others Activity") { nav.popBackStack() }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("FAQ", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Temukan jawaban untuk pertanyaan umum seputar produk dan pemesanan.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            faqs.forEachIndexed { i, (q, a) ->
                var open by remember { mutableStateOf(i == 0) }
                Column(Modifier.fillMaxWidth().clickable { open = !open }.padding(vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(q, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Icon(if (open) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
                    }
                    if (open) Text(a, Modifier.padding(top = 6.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Belum menemukan jawaban?", fontWeight = FontWeight.Bold)
                    Text("Sampaikan pertanyaan Anda langsung melalui WhatsApp.", fontSize = 13.sp)
                    WhatsAppButton({ ctx.openUrl(WA_URL) })
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
