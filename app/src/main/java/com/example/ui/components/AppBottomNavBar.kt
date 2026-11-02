package com.example.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNavTab

@Composable
fun AppBottomNavBar(
    selectedTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    pendingLeavesCount: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .testTag("app_bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        AppNavTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val icon = when (tab) {
                AppNavTab.HOME -> Icons.Default.Home
                AppNavTab.ATTENDANCE -> Icons.Default.Fingerprint
                AppNavTab.LEAVES -> Icons.Default.EventBusy
                AppNavTab.SALARY -> Icons.Default.Payments
                AppNavTab.PROFILE -> Icons.Default.Person
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == AppNavTab.LEAVES && pendingLeavesCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = Color(0xFFF59E0B)) {
                                    Text(
                                        text = "$pendingLeavesCount",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        ) {
                            Icon(imageVector = icon, contentDescription = tab.titleArabic)
                        }
                    } else {
                        Icon(imageVector = icon, contentDescription = tab.titleArabic)
                    }
                },
                label = {
                    Text(
                        text = tab.titleArabic,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = accentColor,
                    indicatorColor = accentColor,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
