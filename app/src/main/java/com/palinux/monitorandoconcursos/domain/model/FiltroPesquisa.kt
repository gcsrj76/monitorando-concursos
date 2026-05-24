package com.palinux.monitorandoconcursos.domain.model

import java.time.LocalDate

data class FiltroPesquisa(
    val ufsOcultadas: Set<String> = emptySet(),
    val dataInscricaoMinima: LocalDate? = null
)