package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun BottomNavBar(currentRoute: String, onNavigate: (String) -> Unit) {
    val items = listOf(
        Triple("home", "Home", Icons.Default.Home),
        Triple("create", "Create", Icons.Default.AutoAwesome),
        Triple("feed", "Feed", Icons.Default.RssFeed),
        Triple("marketplace", "Market", Icons.Default.Storefront),
        Triple("profile", "Profile", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { (route, label, icon) ->
            val selected = currentRoute == route
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = selected,
                onClick = { onNavigate(route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            )
        }
    }
}
