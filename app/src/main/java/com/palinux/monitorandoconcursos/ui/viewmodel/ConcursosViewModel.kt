package com.palinux.monitorandoconcursos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palinux.monitorandoconcursos.data.repository.ConcursoRepository
import com.palinux.monitorandoconcursos.domain.model.Concurso
import com.palinux.monitorandoconcursos.domain.model.FiltroPesquisa
import com.palinux.monitorandoconcursos.domain.model.TipoOrdenacao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.map

class ConcursosViewModel : ViewModel() {

    private val repository = ConcursoRepository()

    private val _todosConcursos = MutableStateFlow<List<Concurso>>(emptyList())

    private val _filtro = MutableStateFlow(FiltroPesquisa())
    val filtro: StateFlow<FiltroPesquisa> = _filtro.asStateFlow()

    private val _estaCarregando = MutableStateFlow(false)
    val estaCarregando: StateFlow<Boolean> = _estaCarregando.asStateFlow()

    private val _ordenacaoAtual = MutableStateFlow(TipoOrdenacao.DATA)
    val ordenacaoAtual: StateFlow<TipoOrdenacao> = _ordenacaoAtual.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // CORRIGIDO: Mapeia corretamente a lista inteira de concursos obtendo as UFs únicas
    val todasUfsDisponiveis: StateFlow<List<String>> = _todosConcursos
        .map { lista ->
            lista.map { it.regiao }.distinct().sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UNIFICADO: Com os tipos explicitados para o compilador não se perder
    val concursosFiltrados: StateFlow<List<Concurso>> = combine(
        _todosConcursos,
        _filtro,
        _ordenacaoAtual
    ) { lista: List<Concurso>, criterio: FiltroPesquisa, ordem: TipoOrdenacao -> // <<< TIPOS EXPLICITADOS AQUI
        lista.filter { concurso ->
            val matchesRegiao = concurso.regiao !in criterio.ufsOcultadas

            val matchesData = if (criterio.dataInscricaoMinima != null) {
                val dataConcurso = tentarMapearData(concurso.dataInscricao)
                if (dataConcurso != null) {
                    dataConcurso.isAfter(criterio.dataInscricaoMinima) || dataConcurso.isEqual(criterio.dataInscricaoMinima)
                } else {
                    true
                }
            } else {
                true
            }

            matchesRegiao && matchesData
        }.let { listaFiltrada ->
            when (ordem) {
                TipoOrdenacao.DATA -> listaFiltrada.sortedBy { tentarMapearData(it.dataInscricao) ?: LocalDate.MAX }
                TipoOrdenacao.VALOR -> listaFiltrada.sortedByDescending { it.salarioMaximo }
                TipoOrdenacao.VAGAS -> listaFiltrada.sortedByDescending { it.vagas }
                TipoOrdenacao.UF -> listaFiltrada.sortedBy { it.regiao }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        carregarDados()
    }

    fun carregarDados() {
        viewModelScope.launch {
            _estaCarregando.value = true
            val resultado = repository.buscarConcursos("https://www.pciconcursos.com.br/concursos/")
            _todosConcursos.value = resultado
            _estaCarregando.value = false
        }
    }

    fun atualizarFiltro(novoFiltro: FiltroPesquisa) {
        _filtro.value = novoFiltro
    }

    fun alternarVisibilidadeUf(uf: String) {
        val ocultadasAtuais = _filtro.value.ufsOcultadas.toMutableSet()
        if (ocultadasAtuais.contains(uf)) {
            ocultadasAtuais.remove(uf)
        } else {
            ocultadasAtuais.add(uf)
        }
        _filtro.value = _filtro.value.copy(ufsOcultadas = ocultadasAtuais)
    }

    fun isolarUfEspecifica(ufSelecionada: String) {
        if (ufSelecionada == "Todas") {
            _filtro.value = _filtro.value.copy(ufsOcultadas = emptySet())
        } else {
            val todasUfsExistentes = todasUfsDisponiveis.value
            val novasOcultadas = todasUfsExistentes.filter { it != ufSelecionada }.toSet()
            _filtro.value = _filtro.value.copy(ufsOcultadas = novasOcultadas)
        }
    }

    fun mudarOrdenacao(novaOrdem: TipoOrdenacao) {
        _ordenacaoAtual.value = novaOrdem
    }

    private fun tentarMapearData(dataStr: String): LocalDate? {
        return try {
            val apenasData = dataStr.substringAfterLast(" ").trim()
            LocalDate.parse(apenasData, dateFormatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }
}