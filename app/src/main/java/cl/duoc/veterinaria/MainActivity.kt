package cl.duoc.veterinaria

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import cl.duoc.veterinaria.service.NotificacionService
import cl.duoc.veterinaria.ui.navigation.NavGraph
import cl.duoc.veterinaria.ui.theme.VeterinariaAppTheme
import cl.duoc.veterinaria.ui.viewmodel.MainViewModel

/**
 * MainActivity es la actividad principal y el punto de entrada de la aplicación.
 */
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            iniciarServicioNotificaciones()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        verificarYPedirPermisos()
        enableEdgeToEdge()
        
        setContent {
            val isDarkMode by mainViewModel.isDarkMode.collectAsState()
            
            VeterinariaAppTheme(darkTheme = isDarkMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                        // PASAMOS el mainViewModel para que toda la app use la misma instancia del tema
                        NavGraph(mainViewModel = mainViewModel)
                    }
                }
            }
        }
    }

    private fun verificarYPedirPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    iniciarServicioNotificaciones()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            iniciarServicioNotificaciones()
        }
    }

    private fun iniciarServicioNotificaciones() {
        val intent = Intent(this, NotificacionService::class.java)
        startService(intent)
    }
}
