package com.attendance.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.app.presentation.theme.AttendanceTheme
import com.attendance.app.presentation.theme.PrimaryGreen
import com.attendance.app.presentation.theme.TextPrimaryLight
import com.attendance.app.presentation.theme.TextSecondaryLight

@Composable
fun StatsCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimaryLight
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview
@Composable
fun StatsCardPreview() {
    AttendanceTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            StatsCard(
                value = "45",
                label = "Total Students",
                modifier = Modifier.weight(1f),
                valueColor = PrimaryGreen
            )
        }
    }
}
