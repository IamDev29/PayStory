package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Shared Design System tokens and foundational UI components for PayStory
 * Applied consistently across all screens (Login, Onboarding, Home, Transactions,
 * Budgets, Analytics, Settings, Main).
 */
object PayStoryTokens {
    // Corner Radii Scale
    val RadiusXs = RoundedCornerShape(4.dp)
    val RadiusSm = RoundedCornerShape(8.dp)
    val RadiusMd = RoundedCornerShape(12.dp)
    val RadiusLg = RoundedCornerShape(16.dp)
    val RadiusXl = RoundedCornerShape(20.dp)
    val Radius2Xl = RoundedCornerShape(24.dp)
    val RadiusFull = RoundedCornerShape(999.dp)

    // Spacing Scale
    val SpaceXs: Dp = 4.dp
    val SpaceSm: Dp = 8.dp
    val SpaceMd: Dp = 12.dp
    val SpaceBase: Dp = 16.dp
    val SpaceLg: Dp = 20.dp
    val SpaceXl: Dp = 24.dp
    val Space2Xl: Dp = 32.dp

    // Border Widths
    val BorderThin: Dp = 1.dp
    val BorderThick: Dp = 2.dp
}

@Composable
fun PayStoryTopAppBar(
    modifier: Modifier = Modifier,
    userInitials: String = "PS",
    notificationCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PayStoryTokens.SpaceLg, vertical = PayStoryTokens.SpaceMd),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo & Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceSm)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(PayStoryTokens.RadiusMd)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✍️",
                    fontSize = 18.sp
                )
            }
            Text(
                text = "PayStory",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Action icons: Notifications & User Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PayStoryTokens.SpaceMd)
        ) {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge(containerColor = MaterialTheme.colorScheme.primary)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(PayStoryTokens.BorderThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onProfileClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = userInitials,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PayStoryPill(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    icon: ImageVector? = null,
    dotColor: Color? = null
) {
    Surface(
        modifier = modifier,
        shape = PayStoryTokens.RadiusFull,
        color = containerColor,
        border = BorderStroke(PayStoryTokens.BorderThin, contentColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor
            )
        }
    }
}
