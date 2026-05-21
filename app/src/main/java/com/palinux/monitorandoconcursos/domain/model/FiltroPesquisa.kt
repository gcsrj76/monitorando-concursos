package com.palinux.monitorandoconcursos.domain.model

import java.time.LocalDate

data class FiltroPesquisa(
    val regiao: String = "",
    val escolaridade: String = "",
    val cargoQuery: String = "",
    val salarioMinimo: Double = 0.0,
    val apenasComVagasImediatas: Boolean = false,
    val dataInscricaoMinima: LocalDate? = null
)