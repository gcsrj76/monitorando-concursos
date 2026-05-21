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
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
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

    // Combina dinamicamente a lista bruta com os critérios de filtro vigentes
    val concursosFiltrados: StateFlow<List<Concurso>> = combine(_todosConcursos, _filtro) { lista, criterio ->
        lista.filter { concurso ->
            val matchesRegiao = criterio.regiao.isEmpty() || concurso.regiao.contains(criterio.regiao, ignoreCase = true)
            val matchesEscolaridade = criterio.escolaridade.isEmpty() || concurso.escolaridade.contains(criterio.escolaridade, ignoreCase = true)
            val matchesCargo = criterio.cargoQuery.isEmpty() || concurso.cargos.contains(criterio.cargoQuery, ignoreCase = true) || concurso.instituicao.contains(criterio.cargoQuery, ignoreCase = true)
            val matchesSalario = concurso.salarioMaximo >= criterio.salarioMinimo
            val matchesVagas = !criterio.apenasComVagasImediatas || !concurso.isCadastroReserva

            // Nova lógica de filtragem por data
            val matchesData = if (criterio.dataInscricaoMinima != null) {
                val dataConcurso = tentarMapearData(concurso.dataInscricao)
                if (dataConcurso != null) {
                    // Verifica se a data do concurso é posterior ou igual à inserida no filtro
                    dataConcurso.isAfter(criterio.dataInscricaoMinima) || dataConcurso.isEqual(criterio.dataInscricaoMinima)
                } else {
                    true // Se não conseguirmos ler a data (ex: "Inscrições prorrogadas"), mantemos na lista por segurança
                }
            } else {
                true
            }

            matchesRegiao && matchesEscolaridade && matchesCargo && matchesSalario && matchesVagas && matchesData
        }.sortedBy { concurso ->
            // ORDENAÇÃO POR DATA: Concursos com prazos mais próximos aparecem primeiro
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
            // URL fictícia ou real mapeada
            val resultado = repository.buscarConcursos("https://www.pciconcursos.com.br/concursos/")
            _todosConcursos.value = resultado
            _estaCarregando.value = false
        }
    }

    fun atualizarFiltro(novoFiltro: FiltroPesquisa) {
        _filtro.value = novoFiltro
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
}