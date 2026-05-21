package com.palinux.monitorandoconcursos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaVisualizadorConcursos(viewModel: ConcursosViewModel = viewModel()) {
    val listaConcursos by viewModel.concursosFiltrados.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val estaCarregando by viewModel.estaCarregando.collectAsState()

    // Controle do diálogo do calendário
    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Buscador de Concursos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp // Opcional: Garante um tamanho robusto e amigável
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Filtros de Pesquisa", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = filtro.cargoQuery,
                        onValueChange = { viewModel.atualizarFiltro(filtro.copy(cargoQuery = it)) },
                        label = { Text("Pesquisar por Cargo ou Instituição") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = filtro.regiao,
                            onValueChange = { viewModel.atualizarFiltro(filtro.copy(regiao = it)) },
                            label = { Text("Região/UF") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = filtro.escolaridade,
                            onValueChange = { viewModel.atualizarFiltro(filtro.copy(escolaridade = it)) },
                            label = { Text("Escolaridade") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- NOVO CAMPO: SELETOR DE DATA ---
                    val dataFormatada = filtro.dataInscricaoMinima?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Qualquer data"

                    OutlinedButton(
                        onClick = { mostrarDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Calendário")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Inscrições posteriores a: $dataFormatada")
                    }

                    // Se o usuário já filtrou por data, mostra a opção de limpar o filtro de data
                    if (filtro.dataInscricaoMinima != null) {
                        TextButton(
                            onClick = { viewModel.atualizarFiltro(filtro.copy(dataInscricaoMinima = null)) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Limpar filtro de data", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = filtro.apenasComVagasImediatas,
                            onCheckedChange = { viewModel.atualizarFiltro(filtro.copy(apenasComVagasImediatas = it)) }
                        )
                        Text(text = "Ocultar Cadastro de Reserva")
                    }
                }
            }

            // --- DIÁLOGO DO CALENDÁRIO NATIVO (M3) ---
            if (mostrarDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { mostrarDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { milissegundos ->
                                // Converte os milissegundos selecionados no calendário para LocalDate
                                val dataSelecionada = Instant.ofEpochMilli(milissegundos)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()

                                viewModel.atualizarFiltro(filtro.copy(dataInscricaoMinima = dataSelecionada))
                            }
                            mostrarDatePicker = false
                        }) {
                            Text("Confirmar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDatePicker = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
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