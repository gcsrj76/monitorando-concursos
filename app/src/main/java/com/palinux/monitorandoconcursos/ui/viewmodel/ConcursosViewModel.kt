package com.palinux.monitorandoconcursos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palinux.monitorandoconcursos.data.repository.ConcursoRepository
import com.palinux.monitorandoconcursos.domain.model.Concurso
import com.palinux.monitorandoconcursos.domain.model.FiltroPesquisa
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

class ConcursosViewModel : ViewModel() {

    private val repository = ConcursoRepository()

    private val _todosConcursos = MutableStateFlow<List<Concurso>>(emptyList())
    private val _filtro = MutableStateFlow(FiltroPesquisa())
    val filtro: StateFlow<FiltroPesquisa> = _filtro.asStateFlow()

    private val _estaCarregando = MutableStateFlow(false)
    val estaCarregando: StateFlow<Boolean> = _estaCarregando.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Expõe todas as UFs distintas encontradas na raspagem atual para a UI gerar os botões
    val todasUfsDisponiveis: StateFlow<List<String>> = combine(_todosConcursos) { lista ->
        lista[0].map { it.regiao }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combina dinamicamente aplicando os novos critérios simplificados
    val concursosFiltrados: StateFlow<List<Concurso>> = combine(_todosConcursos, _filtro) { lista, criterio ->
        lista.filter { concurso ->
            // Se a UF do concurso estiver no conjunto de ignoradas, ela é filtrada para fora
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
        }.sortedBy { concurso ->
            tentarMapearData(concurso.dataInscricao) ?: LocalDate.MAX
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

    // Função utilitária para alternar o estado de visibilidade de uma UF
    fun alternarVisibilidadeUf(uf: String) {
        val ocultadasAtuais = _filtro.value.ufsOcultadas.toMutableSet()
        if (ocultadasAtuais.contains(uf)) {
            ocultadasAtuais.remove(uf)
        } else {
            ocultadasAtuais.add(uf)
        }
        _filtro.value = _filtro.value.copy(ufsOcultadas = ocultadasAtuais)
    }


    private fun tentarMapearData(dataStr: String): LocalDate? {
        return try {
            // Remove textos extras se houver e pega apenas a parte da data dd/MM/yyyy
            val apenasData = dataStr.substringAfterLast(" ").trim()
            LocalDate.parse(apenasData, dateFormatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    fun isolarUfEspecifica(ufSelecionada: String) {
        if (ufSelecionada == "Todas") {
            // Se escolheu "Todas", limpa o filtro e exibe tudo
            _filtro.value = _filtro.value.copy(ufsOcultadas = emptySet())
        } else {
            // Pega todas as UFs que existem na busca atual, exceto a selecionada
            val todasUfsExistentes = todasUfsDisponiveis.value
            val novasOcultadas = todasUfsExistentes.filter { it != ufSelecionada }.toSet()

            _filtro.value = _filtro.value.copy(ufsOcultadas = novasOcultadas)
        }
    }
}