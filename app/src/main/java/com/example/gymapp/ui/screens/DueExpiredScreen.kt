package com.example.gymapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.gymapp.data.Member
import com.example.gymapp.ui.components.MemberAvatar
import com.example.gymapp.util.DateUtils
import com.example.gymapp.viewmodel.DueExpiredUiState
import com.example.gymapp.viewmodel.MemberDueItem
import com.example.ui.theme.StatusDueSoon
import com.example.ui.theme.StatusDueSoonContainer
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusExpiredContainer
import com.example.ui.theme.StatusPaid
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueExpiredScreen(
    uiState: DueExpiredUiState,
    onMarkFeePaid: (Member) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.tab_due_expired))
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState.dueSoon.isEmpty() && uiState.expired.isEmpty()) {
                AllCaughtUpState(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 720.dp),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.dueSoon.isNotEmpty()) {
                        item(key = "header_due_soon") {
                            SectionHeader(
                                title = stringResource(R.string.section_due_soon),
                                count = uiState.dueSoon.size,
                                accentColor = StatusDueSoon,
                                isExpired = false
                            )
                        }
                        items(
                            items = uiState.dueSoon,
                            key = { "due_${it.member.id}" }
                        ) { item ->
                            DueExpiredMemberRow(
                                item = item,
                                isExpired = false,
                                onCallClick = {
                                    val dialIntent = Intent(
                                        Intent.ACTION_DIAL,
                                        Uri.parse("tel:${item.member.phone}")
                                    )
                                    context.startActivity(dialIntent)
                                },
                                onMarkPaidClick = {
                                    onMarkFeePaid(item.member)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "${item.member.name}'s fee moved forward 1 month"
                                        )
                                    }
                                }
                            )
                        }
                    }

                    if (uiState.expired.isNotEmpty()) {
                        item(key = "header_expired") {
                            Spacer(modifier = Modifier.height(4.dp))
                            SectionHeader(
                                title = stringResource(R.string.section_expired),
                                count = uiState.expired.size,
                                accentColor = StatusExpired,
                                isExpired = true
                            )
                        }
                        items(
                            items = uiState.expired,
                            key = { "exp_${it.member.id}" }
                        ) { item ->
                            DueExpiredMemberRow(
                                item = item,
                                isExpired = true,
                                onCallClick = {
                                    val dialIntent = Intent(
                                        Intent.ACTION_DIAL,
                                        Uri.parse("tel:${item.member.phone}")
                                    )
                                    context.startActivity(dialIntent)
                                },
                                onMarkPaidClick = {
                                    onMarkFeePaid(item.member)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "${item.member.name}'s fee moved forward 1 month"
                                        )
                                    }
                                }
                            )
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    accentColor: Color,
    isExpired: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isExpired) Icons.Filled.WarningAmber else Icons.Filled.Schedule,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = accentColor.copy(alpha = 0.14f),
            shape = RoundedCornerShape(999.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun DueExpiredMemberRow(
    item: MemberDueItem,
    isExpired: Boolean,
    onCallClick: () -> Unit,
    onMarkPaidClick: () -> Unit
) {
    val member = item.member
    val badgeBg = if (isExpired) StatusExpiredContainer else StatusDueSoonContainer
    val badgeFg = if (isExpired) StatusExpired else StatusDueSoon

    val daysText = when {
        item.daysDifference < -1L -> "${-item.daysDifference} days overdue"
        item.daysDifference == -1L -> "1 day overdue"
        item.daysDifference == 0L -> "Due today"
        item.daysDifference == 1L -> "1 day left"
        else -> "${item.daysDifference} days left"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("due_card_${member.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MemberAvatar(
                    name = member.name,
                    photoPath = member.photoPath,
                    size = 52.dp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = badgeBg,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = daysText,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeFg,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = member.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Due: ${DateUtils.formatDate(member.feeDueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCallClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("call_member_${member.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.call_member))
                }

                Button(
                    onClick = onMarkPaidClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusPaid,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mark_paid_${member.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.mark_paid_short))
                }
            }
        }
    }
}

@Composable
private fun AllCaughtUpState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(StatusPaid.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.EventAvailable,
                contentDescription = null,
                tint = StatusPaid,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "All fees are up to date",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "No members have fees due within the next 3 days or expired.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
