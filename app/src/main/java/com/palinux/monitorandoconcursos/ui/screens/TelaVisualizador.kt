package com.palinux.monitorandoconcursos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.palinux.monitorandoconcursos.ui.components.ItemConcursoCard
import com.palinux.monitorandoconcursos.ui.viewmodel.ConcursosViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TelaVisualizadorConcursos(viewModel: ConcursosViewModel = viewModel()) {
    val listaConcursos by viewModel.concursosFiltrados.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val todasUfs by viewModel.todasUfsDisponiveis.collectAsState()
    val estaCarregando by viewModel.estaCarregando.collectAsState()

    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Buscador de Concursos", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Card de Filtros Remodelado
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Filtros de Pesquisa", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // --- SELETOR DE DATA ---
                    val dataFormatada = filtro.dataInscricaoMinima?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Qualquer data"
                    OutlinedButton(
                        onClick = { mostrarDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Calendário")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Inscrições posteriores a: $dataFormatada")
                    }

                    if (filtro.dataInscricaoMinima != null) {
                        TextButton(
                            onClick = { viewModel.atualizarFiltro(filtro.copy(dataInscricaoMinima = null)) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Limpar filtro de data", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    // Se existirem UFs localizadas, exibe a seção de chips organizados em linha
                    if (todasUfs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Filtrar por Região (Clique para remover da busca):",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // FlowRow distribui os itens em linha e quebra automaticamente a linha se necessário
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            todasUfs.forEach { uf ->
                                // Só renderiza a UF se ela NÃO tiver sido removida pelo usuário
                                if (uf !in filtro.ufsOcultadas) {
                                    InputChip(
                                        selected = true,
                                        onClick = { viewModel.alternarVisibilidadeUf(uf) },
                                        label = {
                                            Text(
                                                text = uf,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        // Customização para se assemelhar ao chip visual do Card
                                        colors = InputChipDefaults.inputChipColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            labelColor = MaterialTheme.colorScheme.onPrimary
                                        ) ,
                                        modifier = Modifier
                                            .height(24.dp)
                                            .padding(vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        // Se houver qualquer UF oculta, exibe um botão discreto para restaurá-las
                        if (filtro.ufsOcultadas.isNotEmpty()) {
                            TextButton(
                                onClick = { viewModel.atualizarFiltro(filtro.copy(ufsOcultadas = emptySet())) },
                                modifier = Modifier.align(Alignment.Start)
                            ) {
                                Text("Restaurar todas as Regiões", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // --- DIÁLOGO DO CALENDÁRIO NATIVO ---
            if (mostrarDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { mostrarDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { milissegundos ->
                                val dataSelecionada = Instant.ofEpochMilli(milissegundos)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                viewModel.atualizarFiltro(filtro.copy(dataInscricaoMinima = dataSelecionada))
                            }
                            mostrarDatePicker = false
                        }) { Text("Confirmar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
                    }
                ) { DatePicker(state = datePickerState) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estado de carregamento ou lista de resultados
            if (estaCarregando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Text(
                    text = "${listaConcursos.size} concursos encontrados",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listaConcursos) { concurso ->
                        ItemConcursoCard(concurso = concurso)
                    }
                }
            }
        }
    }
}