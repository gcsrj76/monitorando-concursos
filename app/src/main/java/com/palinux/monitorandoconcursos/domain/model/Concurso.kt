package com.palinux.monitorandoconcursos.domain.model

data class Concurso(
    val instituicao: String,
    val regiao: String,
    val vagas: Int,
    val isCadastroReserva: Boolean,
    val salarioMaximo: Double,
    val cargos: String,
    val escolaridade: String,
    val dataInscricao: String
)

