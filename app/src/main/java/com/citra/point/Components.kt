package com.citra.point

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun Context.openUrl(url: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

fun <T> pageItems(list: List<T>, page: Int, size: Int = 4) = list.drop((page - 1) * size).take(size)
fun pageCount(n: Int, size: Int = 4) = maxOf(1, (n + size - 1) / size)

@Composable
fun LogoBar() {
    Column(Modifier.background(MaterialTheme.colorScheme.surface).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp, 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ChangeHistory, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Text("Citra", color = CitraRed, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(" Productions", fontSize = 8.sp, fontFamily = FontFamily.Serif)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun BackRow(text: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onBack).padding(16.dp, 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp)); Text(text, fontSize = 13.sp)
    }
}

@Composable
fun SectionTitle(text: String) = Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)

/** Placeholder gambar. Ganti dengan Image(painterResource(R.drawable.xxx)) atau Coil AsyncImage. */
@Composable
fun ProductImage(modifier: Modifier = Modifier) {
    Box(modifier.background(Color(0xFF8D8478)), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.EmojiEvents, null, Modifier.fillMaxSize(0.5f), tint = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
fun ProductCard(p: Product, fav: Boolean, onFav: () -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick, modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box {
            ProductImage(Modifier.fillMaxWidth().aspectRatio(1f))
            IconButton(onFav, Modifier.align(Alignment.TopEnd)) {
                Icon(if (fav) Icons.Default.Star else Icons.Default.StarBorder, "Favorit", tint = Color.White)
            }
        }
        Column(Modifier.padding(8.dp)) {
            Text(p.categoryName, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(p.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2)
            Text(p.price, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/** Grid 2 kolom tanpa LazyGrid, jadi aman di dalam Column yang bisa di-scroll. */
@Composable
fun ProductGrid(list: List<Product>, favorites: List<Int>, onFav: (Int) -> Unit, onOpen: (Product) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        list.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { p ->
                    ProductCard(p, p.id in favorites, { onFav(p.id) }, { onOpen(p) }, Modifier.weight(1f))
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CategoryCard(c: ProductCategory, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick, modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Category, null, Modifier.size(22.dp))
            }
            Spacer(Modifier.height(6.dp)); Text(c.name, fontSize = 12.sp)
        }
    }
}

@Composable
fun CategoryGrid(selected: String?, onClick: (String) -> Unit) {
    val cs = Sample.categories
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cs.take(3).forEach { c -> CategoryCard(c, c.id == selected, { onClick(c.id) }, Modifier.weight(1f)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryCard(cs[3], cs[3].id == selected, { onClick(cs[3].id) }, Modifier.weight(1f))
            CategoryCard(cs[4], cs[4].id == selected, { onClick(cs[4].id) }, Modifier.weight(2f))
        }
    }
}

@Composable
fun SkeletonBox(modifier: Modifier) {
    val a by rememberInfiniteTransition(label = "sk").animateFloat(
        0.4f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "a"
    )
    Box(modifier.alpha(a).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)))
}

@Composable
fun SkeletonHome() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { repeat(3) { SkeletonBox(Modifier.weight(1f).height(80.dp)) } }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBox(Modifier.weight(1f).height(80.dp)); SkeletonBox(Modifier.weight(2f).height(80.dp))
        }
    }
}

@Composable
fun SkeletonProducts() = Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    repeat(2) { SkeletonBox(Modifier.weight(1f).height(220.dp)) }
}

@Composable
fun Pagination(page: Int, total: Int, onPage: (Int) -> Unit) {
    if (total <= 1) return
    Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
        IconButton({ if (page > 1) onPage(page - 1) }) { Icon(Icons.Default.ChevronLeft, null) }
        (1..total).forEach { i ->
            val sel = i == page
            Box(
                Modifier.padding(2.dp).size(28.dp).clip(RoundedCornerShape(6.dp))
                    .background(if (sel) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onPage(i) },
                contentAlignment = Alignment.Center
            ) {
                Text("$i", fontSize = 12.sp,
                    color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
            }
        }
        IconButton({ if (page < total) onPage(page + 1) }) { Icon(Icons.Default.ChevronRight, null) }
    }
}

@Composable
fun OfflineBanner(onRetry: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WifiOff, null, Modifier.size(14.dp)); Spacer(Modifier.width(6.dp))
                Text("Mode offline · Menampilkan data tersimpan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Belum dapat memuat pembaruan.", fontSize = 11.sp)
                TextButton(onRetry) { Text("Coba lagi", fontSize = 11.sp) }
            }
        }
    }
}

@Composable
fun WhatsAppButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = Color.White)) {
        Icon(Icons.Default.Chat, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
        Text("Chat melalui WhatsApp")
    }
}
