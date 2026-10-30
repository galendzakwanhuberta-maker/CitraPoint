package com.citra.point

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AppViewModel = viewModel()
            CitraTheme(vm.darkTheme) { CitraApp(vm) }
        }
    }
}

fun NavController.goTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true; restoreState = true
}

@Composable
fun CitraApp(vm: AppViewModel) {
    var splash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(1800); splash = false }
    if (splash) SplashScreen() else MainScaffold(vm)
}

@Composable
fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ChangeHistory, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Citra ", color = CitraRed, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 40.sp)
                Text("Point", color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 36.sp)
            }
            Spacer(Modifier.height(24.dp))
            LinearProgressIndicator(Modifier.width(100.dp))
            Text("Memuat data...", fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Powered by", fontSize = 9.sp)
            Text("Citra Productions", color = CitraRed, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MainScaffold(vm: AppViewModel) {
    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route ?: "home"
    val selected = when {
        route == "home" || route.startsWith("category") || route.startsWith("product") -> "home"
        route.startsWith("search") -> "search"
        route == "favorites" -> "favorites"
        else -> "others"
    }
    val tabs = listOf(
        Triple("home", "Home", Icons.Default.Home), Triple("search", "Search", Icons.Default.Search),
        Triple("favorites", "Favorit", Icons.Default.FavoriteBorder), Triple("others", "Others", Icons.Default.MoreHoriz)
    )
    Scaffold(
        contentWindowInsets = WindowInsets(0),   // status bar ditangani LogoBar
        bottomBar = {
            NavigationBar {
                tabs.forEach { (r, label, ic) ->
                    NavigationBarItem(selected == r, { nav.goTab(r) }, { Icon(ic, label) }, label = { Text(label, fontSize = 10.sp) })
                }
            }
        }
    ) { pad ->
        NavHost(nav, "home", Modifier.padding(pad)) {
            composable("home") { HomeScreen(vm, nav) }
            composable("search") { SearchScreen(vm, nav) }
            composable("searchinput") { SearchInputScreen(vm, nav) }
            composable("favorites") { FavoritesScreen(vm, nav) }
            composable("others") { OthersScreen(vm, nav) }
            composable("contact") { ContactScreen(nav) }
            composable("faq") { FaqScreen(nav) }
            composable("category/{id}") { CategoryScreen(it.arguments?.getString("id") ?: "", vm, nav) }
            composable("product/{id}") { ProductDetailScreen(it.arguments?.getString("id")?.toIntOrNull() ?: 1, vm, nav) }
        }
    }
}
