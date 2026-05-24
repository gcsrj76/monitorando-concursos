package com.palinux.monitorandoconcursos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
// ESTAS DUAS IMPORTAÇÕES ABAIXO RESOLVEM O ERRO DO 'by'
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.palinux.monitorandoconcursos.domain.model.TipoOrdenacao
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
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 2.dp)
        ) {
            // Card de Filtros Remodelado
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)) {

                    Text(text = "Filtros de Pesquisa", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    // 1. SELETOR DE DATA
                    val dataFormatada = filtro.dataInscricaoMinima?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Qualquer data"
                    OutlinedButton(
                        onClick = { mostrarDatePicker = true },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        contentPadding = PaddingValues(vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Calendário", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Inscrições posteriores a: $dataFormatada", fontSize = 12.sp)
                    }

                    if (filtro.dataInscricaoMinima != null) {
                        TextButton(
                            onClick = { viewModel.atualizarFiltro(filtro.copy(dataInscricaoMinima = null)) },
                            modifier = Modifier.align(Alignment.End).height(24.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Limpar filtro de data", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        }
                    }

                    // Se existirem UFs localizadas no scraping, exibe o Combo e os Chips
                    if (todasUfs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(4.dp))

                        // --- COMBO DE SELEÇÃO EXCLUSIVA ---
                        var expandido by remember { mutableStateOf(false) }
                        val ufsVisiveis = todasUfs.filter { it !in filtro.ufsOcultadas }
                        val textoCombo = if (ufsVisiveis.size == 1) ufsVisiveis.first() else "Todas as Regiões"

                        Text(text = "Ir direto para uma Região:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))

                        ExposedDropdownMenuBox(
                            expanded = expandido,
                            onExpandedChange = { expandido = !expandido },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = textoCombo,
                                onValueChange = {},
                                readOnly = true,
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                    ),
                                decorationBox = { innerTextField ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp, vertical = 0.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                            innerTextField()
                                        }
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
                                    }
                                }
                            )

                            ExposedDropdownMenu(
                                expanded = expandido,
                                onDismissRequest = { expandido = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todas as Regiões") },
                                    onClick = {
                                        viewModel.isolarUfEspecifica("Todas")
                                        expandido = false
                                    }
                                )
                                todasUfs.forEach { uf ->
                                    DropdownMenuItem(
                                        text = { Text(uf) },
                                        onClick = {
                                            viewModel.isolarUfEspecifica(uf)
                                            expandido = false
                                        }
                                    )
                                }
                            }
                        }

                        // 3. CHIPS DE EXCLUSÃO MANUAL
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Regiões ativas (Clique para remover individualmente):",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val interactionSource = remember { MutableInteractionSource() }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            todasUfs.forEach { uf ->
                                if (uf !in filtro.ufsOcultadas) {
                                    Surface(
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(horizontal = 1.dp, vertical = 1.dp)
                                            .clickable(
                                                interactionSource = interactionSource,
                                                indication = null
                                            ) {
                                                viewModel.alternarVisibilidadeUf(uf)
                                            }
                                    ) {
                                        Text(
                                            text = uf,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

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

            Spacer(modifier = Modifier.height(6.dp))

            // --- CARD DE ORDENAÇÃO COMPACTO ---
            val ordenacaoAtiva by viewModel.ordenacaoAtual.collectAsState()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ordenar por:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    val opcoes = listOf(
                        TipoOrdenacao.DATA to "Data",
                        TipoOrdenacao.VALOR to "Valor",
                        TipoOrdenacao.VAGAS to "Vagas",
                        TipoOrdenacao.UF to "UF"
                    )

                    opcoes.forEach { (tipo, rotulo) ->
                        val isSelecionado = ordenacaoAtiva == tipo

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .background(
                                    color = if (isSelecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                )
                                .clickable { viewModel.mudarOrdenacao(tipo) }
                        ) {
                            Text(
                                text = rotulo,
                                fontSize = 11.sp,
                                fontWeight = if (isSelecionado) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelecionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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