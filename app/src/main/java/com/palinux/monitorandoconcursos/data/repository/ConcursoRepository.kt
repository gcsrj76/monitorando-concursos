package com.palinux.monitorandoconcursos.data.repository

import com.palinux.monitorandoconcursos.domain.model.Concurso
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.text.NumberFormat
import java.util.Locale

class ConcursoRepository {

    suspend fun buscarConcursos(url: String): List<Concurso> = withContext(Dispatchers.IO) {
        val listaConcursos = mutableListOf<Concurso>()
        try {
            // Conecta ao site e baixa o HTML
            val doc: Document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .get()

            // Seleciona todos os blocos de concursos (classes .da e .na)
            val elementosBloco = doc.select("div.da, div.na")

            for (elemento in elementosBloco) {
                // 1. Extração da Instituição
                val instituicao = elemento.select("div.ca > a").text().trim()

                // 2. Extração da Região (Se vazio na div, tenta buscar do contexto anterior/Nacional)
                var regiao = elemento.select("div.cc").text().trim()
                if (regiao.isEmpty()) {
                    // Verifica se herda contexto de bloco nacional
                    val isNacional = elemento.selectXpath("preceding-sibling::h2[1]").text().contains("NACIONAL", ignoreCase = true)
                    regiao = if (isNacional) "Nacional" else "Outros"
                }

                // 3. Extração da div informativa de vagas, valores, cargos e escolaridade
                val informacoesDiv = elemento.select("div.cd").first()
                val textoPrincipalDiv = informacoesDiv?.ownText() ?: "" // Pega apenas o texto direto da div, ignorando spans

                // Parse de vagas e salários
                val (vagas, isCr) = extrairVagas(textoPrincipalDiv)
                val salario = extrairSalario(textoPrincipalDiv)

                // 4. Extração de Cargos e Escolaridade de dentro dos spans internos
                val spans = informacoesDiv?.select("span") ?: org.jsoup.select.Elements()
                val cargos = if (spans.isNotEmpty()) spans[0].ownText().trim() else ""
                val escolaridade = if (spans.size > 1) spans[1].text().trim() else ""

                // 5. Extração da Data de Inscrição
                val dataInscricao = elemento.select("div.ce > span").text().replace("\n", " ").trim()

                if (instituicao.isNotEmpty()) {
                    listaConcursos.add(
                        Concurso(
                            instituicao = instituicao,
                            regiao = regiao,
                            vagas = vagas,
                            isCadastroReserva = isCr,
                            salarioMaximo = salario,
                            cargos = cargos,
                            escolaridade = escolaridade,
                            dataInscricao = dataInscricao
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext listaConcursos
    }

    private fun extrairVagas(texto: String): Pair<Int, Boolean> {
        if (texto.contains("Cadastro de Reserva", ignoreCase = true)) {
            return Pair(0, true)
        }
        val regex = "(\\d+)\\s+vagas".toRegex()
        val match = regex.find(texto)
        val numeroVagas = match?.groupValues?.get(1)?.toIntOrNull() ?: 0
        return Pair(numeroVagas, false)
    }

    private fun extrairSalario(texto: String): Double {
        val regex = "até\\s+R\\$\\s*([\\d.,]+)".toRegex()
        val match = regex.find(texto)
        val salarioStr = match?.groupValues?.get(1) ?: return 0.0

        return try {
            // Normaliza formato brasileiro (ex: 15.659,70 -> 15659.70)
            val format = NumberFormat.getInstance(Locale("pt", "BR"))
            format.parse(salarioStr)?.toDouble() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }
}