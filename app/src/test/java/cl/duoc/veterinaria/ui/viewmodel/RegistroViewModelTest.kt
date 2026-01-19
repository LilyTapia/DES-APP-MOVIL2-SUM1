package cl.duoc.veterinaria.ui.viewmodel

import android.content.Context
import cl.duoc.veterinaria.data.IVeterinariaRepository
import cl.duoc.veterinaria.data.local.entities.ConsultaEntity
import cl.duoc.veterinaria.data.local.entities.MascotaEntity
import cl.duoc.veterinaria.data.local.entities.PedidoEntity
import cl.duoc.veterinaria.data.local.entities.UsuarioEntity
import cl.duoc.veterinaria.model.Medicamento
import cl.duoc.veterinaria.model.TipoServicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelTest {

    private lateinit var viewModel: RegistroViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockRepository = object : IVeterinariaRepository {
        override val totalMascotasRegistradas: StateFlow<Int> = MutableStateFlow(0)
        override val totalConsultasRealizadas: StateFlow<Int> = MutableStateFlow(0)
        override val nombreUltimoDueno: StateFlow<String> = MutableStateFlow("")
        override val listaMascotas: StateFlow<List<String>> = MutableStateFlow(emptyList())
        override val ultimaAtencionTipo: StateFlow<String?> = MutableStateFlow(null)
        override val mascotasLocal: Flow<List<MascotaEntity>> = flowOf(emptyList())
        override val consultasLocal: Flow<List<ConsultaEntity>> = flowOf(emptyList())
        override val usuariosLocal: Flow<List<UsuarioEntity>> = flowOf(emptyList())
        override val pedidosLocal: Flow<List<PedidoEntity>> = flowOf(emptyList())

        override fun registrarAtencion(
            nombreDueno: String,
            cantidadMascotas: Int,
            nombreMascota: String,
            especieMascota: String,
            tipoServicio: String?,
            edad: Int,
            peso: Double,
            consultaId: String?,
            fechaHora: String?,
            veterinario: String?,
            costo: Double
        ) {
            (ultimaAtencionTipo as MutableStateFlow).value = tipoServicio
        }

        override fun init(context: Context) {}
        override fun eliminarMascota(nombreMascota: String) {}

        override fun editarMascota(textoOriginal: String, textoNuevo: String) {}

        override suspend fun agregarMascotaRoom(nombre: String, especie: String, edad: Int, peso: Double, dueno: String) {}

        override suspend fun eliminarMascotaRoom(mascota: MascotaEntity) {}

        override suspend fun agregarConsultaRoom(consulta: ConsultaEntity) {}

        override suspend fun registrarUsuario(nombre: String, email: String, pass: String): UsuarioEntity {
            return UsuarioEntity(nombreUsuario = nombre, email = email, pass = pass)
        }

        override suspend fun buscarUsuario(email: String, user: String): UsuarioEntity? = null

        override suspend fun registrarPedidoRoom(pedido: PedidoEntity) {}
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegistroViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateDatosDueno should correctly update the state`() = runTest {
        val nombre = "Juan Perez"
        val telefono = "123456789"
        val email = "juan@test.com"

        viewModel.updateDatosDueno(nombre, telefono, email)

        val state = viewModel.uiState.value
        assertEquals(nombre, state.duenoNombre)
        assertEquals(telefono, state.duenoTelefono)
        assertEquals(email, state.duenoEmail)
    }

    @Test
    fun `agregarMedicamentoAlCarrito should increment quantity if item already exists`() = runTest {
        val medicamento = Medicamento("Test Med", 100, 1000.0)

        viewModel.agregarMedicamentoAlCarrito(medicamento)
        viewModel.agregarMedicamentoAlCarrito(medicamento)

        val state = viewModel.uiState.value
        assertEquals(1, state.carrito.size)
        assertEquals(2, state.carrito.first().cantidad)
    }

    @Test
    fun `procesarRegistro should generate consulta correctly and update service state`() = runTest {
        viewModel.updateDatosDueno("Test", "123", "test@mail.com")
        viewModel.updateDatosMascota("Bobby", "Perro", "5", "10.0")
        viewModel.updateTipoServicio(TipoServicio.CONTROL)

        assertTrue(viewModel.serviceState.value is ServiceState.Idle)

        viewModel.procesarRegistro()
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNotNull(uiState.consultaRegistrada)
        // La descripción de CONTROL es "Control Sano", por lo que el resultado es "Atención de Control Sano"
        assertEquals("Atención de Control Sano", uiState.consultaRegistrada?.descripcion)

        assertTrue(viewModel.serviceState.value is ServiceState.Stopped)
    }

    @Test
    fun `clearData should reset ui and service states`() = runTest {
        viewModel.updateDatosDueno("Juan", "123", "juan@test.com")
        viewModel.updateDatosMascota("Bobby", "Perro", "5", "10.0")
        
        viewModel.clearData()

        val uiState = viewModel.uiState.value
        assertEquals(null, uiState.consultaRegistrada)
        assertEquals("", uiState.duenoNombre)
        assertEquals("", uiState.mascotaNombre)
        assertTrue(viewModel.serviceState.value is ServiceState.Idle)
    }
}
