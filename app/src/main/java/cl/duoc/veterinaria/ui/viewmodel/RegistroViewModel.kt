package cl.duoc.veterinaria.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.veterinaria.data.IVeterinariaRepository
import cl.duoc.veterinaria.data.VeterinariaRepository
import cl.duoc.veterinaria.data.local.entities.PedidoEntity
import cl.duoc.veterinaria.model.Cliente
import cl.duoc.veterinaria.model.Consulta
import cl.duoc.veterinaria.model.DetallePedido
import cl.duoc.veterinaria.model.EstadoConsulta
import cl.duoc.veterinaria.model.Mascota
import cl.duoc.veterinaria.model.Medicamento
import cl.duoc.veterinaria.model.MedicamentoPromocional
import cl.duoc.veterinaria.model.Pedido
import cl.duoc.veterinaria.model.TipoServicio
import cl.duoc.veterinaria.model.Veterinario
import cl.duoc.veterinaria.service.AgendaVeterinario
import cl.duoc.veterinaria.service.ConsultaService
import cl.duoc.veterinaria.service.MascotaService
import cl.duoc.veterinaria.ui.registro.RegistroUiState
import cl.duoc.veterinaria.util.esDecimalValido
import cl.duoc.veterinaria.util.esNumeroValido
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RegistroViewModel(
    private val repository: IVeterinariaRepository = VeterinariaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    private val _serviceState = MutableStateFlow<ServiceState>(ServiceState.Idle)
    val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()

    val catalogoMedicamentos = listOf(
        Medicamento("Antibiótico Generico", 500, 15000.0),
        Medicamento("Analgésico Básico", 200, 8000.0),
        MedicamentoPromocional("Antiinflamatorio Premium", 200, 25000.0, 0.20),
        MedicamentoPromocional("Vitaminas Caninas", 100, 12000.0, 0.10)
    )

    fun updateDatosDueno(nombre: String? = null, telefono: String? = null, email: String? = null) {
        _uiState.update { it.copy(
            duenoNombre = nombre ?: it.duenoNombre,
            duenoTelefono = telefono ?: it.duenoTelefono,
            duenoEmail = email ?: it.duenoEmail) }
    }

    fun updateDatosMascota(nombre: String? = null, especie: String? = null, edad: String? = null, peso: String? = null, ultimaVacuna: String? = null) {
        _uiState.update { currentState ->
            val nuevoEdad = if (edad != null && (edad.isEmpty() || edad.esNumeroValido())) edad else currentState.mascotaEdad
            val nuevoPeso = if (peso != null && (peso.isEmpty() || peso.esDecimalValido())) peso else currentState.mascotaPeso
            currentState.copy(
                mascotaNombre = nombre ?: currentState.mascotaNombre,
                mascotaEspecie = especie ?: currentState.mascotaEspecie,
                mascotaEdad = nuevoEdad,
                mascotaPeso = nuevoPeso,
                mascotaUltimaVacuna = ultimaVacuna ?: currentState.mascotaUltimaVacuna
            )
        }
    }

    fun updateTipoServicio(servicio: TipoServicio) {
        _uiState.update { it.copy(tipoServicio = servicio) }
    }

    fun agregarMedicamentoAlCarrito(medicamento: Medicamento) {
        _uiState.update { currentState ->
            val carritoActual = currentState.carrito.toMutableList()
            val index = carritoActual.indexOfFirst { it.medicamento.nombre == medicamento.nombre }
            if (index != -1) {
                val detalleExistente = carritoActual[index]
                carritoActual[index] = detalleExistente.copy(cantidad = detalleExistente.cantidad + 1)
            } else {
                carritoActual.add(DetallePedido(medicamento, 1))
            }
            currentState.copy(carrito = carritoActual)
        }
    }

    fun quitarMedicamentoDelCarrito(medicamento: Medicamento) {
        _uiState.update { currentState ->
            val carritoActual = currentState.carrito.toMutableList()
            val index = carritoActual.indexOfFirst { it.medicamento.nombre == medicamento.nombre }
            if (index != -1) {
                val detalleExistente = carritoActual[index]
                if (detalleExistente.cantidad > 1) {
                    carritoActual[index] = detalleExistente.copy(cantidad = detalleExistente.cantidad - 1)
                } else {
                    carritoActual.removeAt(index)
                }
            }
            currentState.copy(carrito = carritoActual)
        }
    }

    fun procesarRegistro() {
        val currentState = _uiState.value
        if (currentState.consultaRegistrada != null || _serviceState.value is ServiceState.Running) return

        viewModelScope.launch {
            _serviceState.value = ServiceState.Running("Calculando costos...")
            delay(1500)
            _serviceState.value = ServiceState.Running("Confirmando reserva y pedido...")
            delay(1500)

            val nombreCliente = if (currentState.duenoNombre.isBlank()) "Venta Mostrador" else currentState.duenoNombre
            val esSoloFarmacia = currentState.mascotaNombre.isBlank()

            val nuevoPedido = if (currentState.carrito.isNotEmpty()) {
                Pedido(Cliente(nombreCliente, "", ""), currentState.carrito)
            } else null

            if (nuevoPedido != null) {
                val itemsTexto = nuevoPedido.detalles.joinToString { "${it.medicamento.nombre} x${it.cantidad}" }
                val fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
                repository.registrarPedidoRoom(
                    PedidoEntity(duenoNombre = nombreCliente, items = itemsTexto, total = nuevoPedido.total, fecha = fechaActual, esCompraDirecta = esSoloFarmacia)
                )
            }

            var consultaFinal: Consulta? = null

            if (!esSoloFarmacia) {
                // Obtener consultas existentes para evitar choques
                val consultasExistentes = repository.consultasLocal.first()
                
                // Buscar siguiente disponibilidad real
                val (veterinarioAsignado, fechaHoraReal) = AgendaVeterinario.buscarSiguienteDisponible(
                    consultasExistentes, 
                    Clock.systemDefaultZone()
                )
                
                val fechaHoraFormateada = AgendaVeterinario.fmt(fechaHoraReal)
                val servicio = currentState.tipoServicio ?: TipoServicio.CONTROL
                val costoConsulta = ConsultaService.calcularCostoBase(servicio, 30)
                val idCita = "AGENDA-" + (1000..9999).random()

                repository.registrarAtencion(
                    nombreDueno = nombreCliente,
                    cantidadMascotas = 1,
                    nombreMascota = currentState.mascotaNombre,
                    especieMascota = currentState.mascotaEspecie,
                    tipoServicio = servicio.descripcion,
                    edad = currentState.mascotaEdad.toIntOrNull() ?: 0,
                    peso = currentState.mascotaPeso.toDoubleOrNull() ?: 0.0,
                    consultaId = idCita,
                    fechaHora = fechaHoraFormateada,
                    veterinario = veterinarioAsignado.nombre,
                    costo = costoConsulta
                )
                
                consultaFinal = Consulta(
                    idConsulta = idCita,
                    descripcion = "Atención de ${servicio.descripcion}",
                    costoConsulta = costoConsulta,
                    fechaAtencion = fechaHoraReal,
                    veterinarioAsignado = Veterinario(veterinarioAsignado.nombre, "")
                )
            }

            _uiState.update { it.copy(
                consultaRegistrada = consultaFinal,
                pedidoRegistrado = nuevoPedido,
                notificacionAutomaticaMostrada = true
            ) }
            _serviceState.value = ServiceState.Stopped
        }
    }

    fun onNotificacionMostrada() {
        _uiState.update { it.copy(notificacionAutomaticaMostrada = false) }
    }

    fun clearData() {
        _uiState.value = RegistroUiState()
        _serviceState.value = ServiceState.Idle
    }
}
