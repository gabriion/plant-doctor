package com.plantdoctor.ui.diagnosis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.plantdoctor.data.local.entity.PlantEntity
import com.plantdoctor.data.remote.model.PlantDiagnosis
import com.plantdoctor.ui.home.SeverityBadge
import com.plantdoctor.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisResultScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: DiagnosisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val plants by viewModel.plants.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnosis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is DiagnosisUiState.Idle -> {
                    // Should not normally be seen
                }

                is DiagnosisUiState.Analyzing -> {
                    AnalyzingContent()
                }

                is DiagnosisUiState.Success -> {
                    SuccessContent(
                        diagnosis = state.diagnosis,
                        imageUri = state.imageUri,
                        plants = plants,
                        onSave = { plantId -> viewModel.saveDiagnosis(plantId) },
                        onNavigateToHome = onNavigateToHome
                    )
                }

                is DiagnosisUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.retry() },
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyzingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 6.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Analyzing your plant...",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This may take a few seconds",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SuccessContent(
    diagnosis: PlantDiagnosis,
    imageUri: String,
    plants: List<PlantEntity>,
    onSave: (Long?) -> Unit,
    onNavigateToHome: () -> Unit
) {
    var saved by remember { mutableStateOf(false) }
    var selectedPlantId by remember { mutableStateOf<Long?>(null) }
    var showPlantDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Plant image
        AsyncImage(
            model = imageUri,
            contentDescription = "Plant photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Identification
            Text(
                text = diagnosis.identification,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Severity and confidence row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SeverityBadge(severity = diagnosis.severity.name)
                ConfidenceIndicator(confidence = diagnosis.confidence.name)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Diagnosis description
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Diagnosis",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = diagnosis.diagnosis,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Treatment plan
            Text(
                text = "Treatment Plan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Immediate Actions
            ExpandableSection(
                title = "Immediate Actions",
                icon = Icons.Filled.Warning,
                iconTint = SeverityModerate,
                items = diagnosis.treatment.immediate
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Recommended Products
            ExpandableSection(
                title = "Recommended Products",
                icon = Icons.Filled.ShoppingCart,
                iconTint = MaterialTheme.colorScheme.primary,
                items = diagnosis.treatment.products
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Prevention
            ExpandableSection(
                title = "Prevention",
                icon = Icons.Filled.Shield,
                iconTint = ConfidenceHigh,
                items = diagnosis.treatment.prevention
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save options
            if (!saved) {
                // Save to plant dropdown
                if (plants.isNotEmpty()) {
                    Box {
                        OutlinedButton(
                            onClick = { showPlantDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Eco, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (selectedPlantId != null) {
                                    "Save to: ${plants.find { it.id == selectedPlantId }?.name ?: "Select plant"}"
                                } else {
                                    "Link to a Plant"
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = showPlantDropdown,
                            onDismissRequest = { showPlantDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("No plant (save independently)") },
                                onClick = {
                                    selectedPlantId = null
                                    showPlantDropdown = false
                                }
                            )
                            plants.forEach { plant ->
                                DropdownMenuItem(
                                    text = { Text(plant.name) },
                                    onClick = {
                                        selectedPlantId = plant.id
                                        showPlantDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        onSave(selectedPlantId)
                        saved = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to Journal")
                }
            } else {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saved to journal!")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onNavigateToHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Home")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    items: List<String>
) {
    var expanded by remember { mutableStateOf(true) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Surface(
                onClick = { expanded = !expanded },
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    if (items.isEmpty()) {
                        Text(
                            "No recommendations available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        items.forEach { item ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    "•",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfidenceIndicator(confidence: String) {
    val (color, stars) = when (confidence.uppercase()) {
        "HIGH" -> ConfidenceHigh to 3
        "MEDIUM" -> ConfidenceMedium to 2
        "LOW" -> ConfidenceLow to 1
        else -> ConfidenceLow to 1
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(stars) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        repeat(3 - stars) {
            Icon(
                Icons.Filled.StarBorder,
                contentDescription = null,
                tint = color.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = confidence.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Analysis Failed",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onNavigateBack) {
            Text("Go Back")
        }
    }
}


