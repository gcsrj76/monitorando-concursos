package com.palinux.monitorandoconcursos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.palinux.monitorandoconcursos.ui.screens.TelaVisualizadorConcursos
import com.palinux.monitorandoconcursos.ui.theme.MonitorandoConcursosTheme
import com.palinux.monitorandoconcursos.ui.viewmodel.ConcursosViewModel

class MainActivity : ComponentActivity() {

    // Instancia a ViewModel usando o delegate 'by viewModels()' do ciclo de vida do Android
    private val concursosViewModel: ConcursosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // MonitorandoConcursosTheme é o tema padrão gerado pelo Android Studio.
            // Se o seu projeto tiver outro nome, ajuste para o nome correto do seu tema.
            MonitorandoConcursosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Chamamos a sua tela do Compose e passamos a nossa ViewModel instanciada
                    TelaVisualizadorConcursos(viewModel = concursosViewModel)
                }
            }
        }
    }
}