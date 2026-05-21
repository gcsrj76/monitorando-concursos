package com.palinux.monitorandoconcursos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.palinux.monitorandoconcursos.domain.model.Concurso

@Composable
fun ItemConcursoCard(concurso: Concurso) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // Adiciona uma borda sutil para destacar os cards no fundo escuro
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = concurso.instituicao,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(
                    onClick = { },
                    label = { Text(concurso.regiao) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Cargos: ${concurso.cargos}", fontSize = 14.sp)
            Text(text = "Escolaridade: ${concurso.escolaridade}", fontSize = 14.sp)

            val textoVagas = if (concurso.isCadastroReserva) "Cadastro de Reserva" else "${concurso.vagas} vagas"
            Text(text = "Vagas: $textoVagas", fontSize = 14.sp, fontWeight = FontWeight.Medium)

            if (concurso.salarioMaximo > 0.0) {
                Text(
                    text = "Remuneração: até R$ %.2f".format(concurso.salarioMaximo),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Inscrições até: ${concurso.dataInscricao}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}