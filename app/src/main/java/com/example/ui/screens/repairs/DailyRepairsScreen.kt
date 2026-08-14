package com.example.ui.screens.repairs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.locale.stringRes
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DailyRepairsScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val repairs by viewModel.repairsState.collectAsStateWithLifecycle()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val currency = profile.currency

    var selectedCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        })
    }

    val startOfDay = selectedCalendar.timeInMillis
    val endOfDay = startOfDay + 86400000L

    val dailyRepairs = repairs.filter { it.dateReceived in startOfDay until endOfDay }

    val totalJobs = dailyRepairs.size
    val totalEstimated = dailyRepairs.sumOf { it.repairCost }
    val totalAdvance = dailyRepairs.sumOf { it.advancePayment }
    val totalRemaining = dailyRepairs.sumOf { it.remainingPayment }

    val formattedDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(startOfDay))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F11))
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringRes("daily_repairs"),
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Date Selector Ribbon
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = selectedCalendar.timeInMillis - 86400000L
                        }
                        selectedCalendar = newCal
                    }
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = GoldPrimary)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedDate,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = selectedCalendar.timeInMillis + 86400000L
                        }
                        selectedCalendar = newCal
                    }
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = GoldPrimary)
                }
            }
        }

        // Daily Financial Summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Jobs Received", color = TextMuted, fontSize = 11.sp)
                    Text(text = "$totalJobs", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(DarkBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Advance Income", color = TextMuted, fontSize = 11.sp)
                    Text(text = "${String.format("%.0f", totalAdvance)} $currency", color = SuccessGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(DarkBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Bal Pending", color = TextMuted, fontSize = 11.sp)
                    Text(text = "${String.format("%.0f", totalRemaining)} $currency", color = DangerRed, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Daily List
        if (dailyRepairs.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Build,
                title = "No Repairs on this Date",
                message = "No mobile repairs were received on $formattedDate."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dailyRepairs, key = { it.id }) { repair ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${repair.jobCode} • ${repair.mobileBrand} ${repair.mobileModel}",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                StatusBadge(status = repair.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Customer: ${repair.customerName} (${repair.customerPhone})",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Problem: ${repair.customerProblem}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Cost: ${repair.repairCost} $currency", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Adv: ${repair.advancePayment}", color = SuccessGreen, fontSize = 12.sp)
                                Text(text = "Bal: ${repair.remainingPayment}", color = DangerRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
