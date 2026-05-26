package com.palinux.monitorandoconcursos.ui.components

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.palinux.monitorandoconcursos.domain.model.Concurso
import com.palinux.monitorandoconcursos.ui.viewmodel.ConcursosViewModel

@Composable
fun ItemConcursoCard(concurso: Concurso, viewModel: ConcursosViewModel) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!concurso.link.isNullOrBlank()) {
                    uriHandler.openUri(concurso.link)
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 1ª LINHA: Instituição e Região
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = concurso.instituicao,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(
                    onClick = { viewModel.isolarUfEspecifica(concurso.regiao) },
                    label = { Text(concurso.regiao) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Detalhes do Concurso
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

            // LINHA FINAL: Inscrições (Esquerda) e Compartilhar WhatsApp (Direita)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // Alinha o texto e o botão na mesma linha horizontal
            ) {
                Text(
                    text = "Inscrições até: ${concurso.dataInscricao}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                AssistChip(
                    onClick = {
                        val textoVagasMsg = if (concurso.isCadastroReserva) "Cadastro de Reserva" else "${concurso.vagas} vagas"
                        val textoMensagem = """
                            📝 *Concurso:* ${concurso.instituicao}
                            📍 *Região:* ${concurso.regiao}
                            💼 *Cargos:* ${concurso.cargos}
                            🎓 *Escolaridade:* ${concurso.escolaridade}
                            🎯 *Vagas:* $textoVagasMsg
                            📅 *Inscrições até:* ${concurso.dataInscricao}
                            
                            🔗 Confira mais detalhes aqui: ${concurso.link}
                        """.trimIndent()

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, textoMensagem)
                            type = "text/plain"
                            `package` = "com.whatsapp"
                        }

                        try {
                            context.startActivity(sendIntent)
                        } catch (e: Exception) {
                            sendIntent.`package` = null
                            val shareIntent = Intent.createChooser(sendIntent, "Compartilhar concurso")
                            context.startActivity(shareIntent)
                        }
                    },
                    label = { Text("WhatsApp", fontSize = 11.sp) }, // Fonte ligeiramente menor para a barra inferior
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartilhar",
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = MaterialTheme.colorScheme.primary,
                        leadingIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}