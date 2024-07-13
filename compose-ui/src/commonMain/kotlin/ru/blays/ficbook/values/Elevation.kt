package ru.blays.ficbook.values

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val FloatingActionButtonDefaults.ZeroElevation: FloatingActionButtonElevation
    @Composable get() = elevation(0.dp, 0.dp, 0.dp, 0.dp)

val CardDefaults.ZeroElevation: CardElevation
    @Composable get() = cardElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp, 0.dp)