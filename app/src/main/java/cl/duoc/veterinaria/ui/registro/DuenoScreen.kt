package cl.duoc.veterinaria.ui.registro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.veterinaria.R
import cl.duoc.veterinaria.ui.auth.LoginViewModel
import cl.duoc.veterinaria.ui.viewmodel.RegistroViewModel
import cl.duoc.veterinaria.util.ValidationUtils

@Composable
fun DuenoScreen(
    viewModel: RegistroViewModel, 
    onNextClicked: () -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val loginState by loginViewModel.uiState.collectAsState()
    
    // CORRECCIÓN: Autocompleta SIEMPRE con el usuario que tiene la sesión iniciada
    LaunchedEffect(loginState.currentUser) {
        val user = loginState.currentUser
        if (user != null) {
            viewModel.updateDatosDueno(
                nombre = user.nombreUsuario,
                email = user.email
            )
        }
    }

    val isNombreValido = ValidationUtils.isValidNombre(uiState.duenoNombre)
    val isTelefonoValido = uiState.duenoTelefono.isNotBlank() && uiState.duenoTelefono.all { it.isDigit() }
    val isEmailValido = ValidationUtils.isValidEmail(uiState.duenoEmail)
    
    val isFormValid = isNombreValido && isTelefonoValido && isEmailValido

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        Image(
            painter = painterResource(id = R.drawable.perrito),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Información de Contacto", 
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Confirme sus datos para avisos sobre su mascota", 
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        RegistroTextField(
            value = uiState.duenoNombre,
            label = "Nombre del responsable",
            onValueChange = { viewModel.updateDatosDueno(nombre = it) },
            isError = !isNombreValido && uiState.duenoNombre.isNotBlank(),
            errorMessage = "Nombre obligatorio"
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        RegistroTextField(
            value = uiState.duenoTelefono,
            label = "Teléfono de contacto (urgencias)",
            onValueChange = { 
                if (it.all { char -> char.isDigit() }) {
                    viewModel.updateDatosDueno(telefono = it) 
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = !isTelefonoValido && uiState.duenoTelefono.isNotBlank(),
            errorMessage = "Ingrese un número válido"
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        RegistroTextField(
            value = uiState.duenoEmail,
            label = "Correo electrónico para reportes",
            onValueChange = { viewModel.updateDatosDueno(email = it) },
            isError = !isEmailValido && uiState.duenoEmail.isNotBlank(),
            errorMessage = "Email inválido",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNextClicked,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = isFormValid
        ) {
            Text("Continuar a datos de mascota", fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
