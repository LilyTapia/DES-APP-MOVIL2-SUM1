package cl.duoc.veterinaria.data

import android.content.Context
import android.content.SharedPreferences
import cl.duoc.veterinaria.data.local.VeterinariaDatabase
import cl.duoc.veterinaria.data.local.entities.ConsultaEntity
import cl.duoc.veterinaria.data.local.entities.MascotaEntity
import cl.duoc.veterinaria.data.local.entities.PedidoEntity
import cl.duoc.veterinaria.data.local.entities.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Define el contrato para el repositorio de la veterinaria.
 */
interface IVeterinariaRepository {
    val totalMascotasRegistradas: StateFlow<Int>
    val totalConsultasRealizadas: StateFlow<Int>
    val nombreUltimoDueno: StateFlow<String>
    val listaMascotas: StateFlow<List<String>>
    val ultimaAtencionTipo: StateFlow<String?>
    val mascotasLocal: Flow<List<MascotaEntity>>
    val consultasLocal: Flow<List<ConsultaEntity>>
    val usuariosLocal: Flow<List<UsuarioEntity>>
    val pedidosLocal: Flow<List<PedidoEntity>>

    fun registrarAtencion(nombreDueno: String, cantidadMascotas: Int, nombreMascota: String, especieMascota: String, tipoServicio: String? = null, edad: Int = 0, peso: Double = 0.0, consultaId: String? = null, fechaHora: String? = null, veterinario: String? = null, costo: Double = 0.0)
    
    fun init(context: Context)

    fun eliminarMascota(nombreMascota: String)
    fun editarMascota(textoOriginal: String, textoNuevo: String)
    
    suspend fun agregarMascotaRoom(nombre: String, especie: String, edad: Int, peso: Double, dueno: String)
    suspend fun eliminarMascotaRoom(mascota: MascotaEntity)
    
    suspend fun agregarConsultaRoom(consulta: ConsultaEntity)

    suspend fun registrarUsuario(nombre: String, email: String, pass: String): UsuarioEntity
    suspend fun buscarUsuario(email: String, user: String): UsuarioEntity?
    
    suspend fun registrarPedidoRoom(pedido: PedidoEntity)
}

/**
 * Implementación Singleton del repositorio.
 */
object VeterinariaRepository : IVeterinariaRepository {
    private val _totalMascotasRegistradas = MutableStateFlow(0)
    private val _totalConsultasRealizadas = MutableStateFlow(0)
    private val _nombreUltimoDueno = MutableStateFlow("N/A")
    private val _listaMascotas = MutableStateFlow<List<String>>(emptyList())
    private val _ultimaAtencionTipo = MutableStateFlow<String?>(null)
    
    private var prefs: SharedPreferences? = null
    private var database: VeterinariaDatabase? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isInitialized = false

    override val totalMascotasRegistradas: StateFlow<Int> = _totalMascotasRegistradas.asStateFlow()
    override val totalConsultasRealizadas: StateFlow<Int> = _totalConsultasRealizadas.asStateFlow()
    override val nombreUltimoDueno: StateFlow<String> = _nombreUltimoDueno.asStateFlow()
    override val listaMascotas: StateFlow<List<String>> = _listaMascotas.asStateFlow()
    override val ultimaAtencionTipo: StateFlow<String?> = _ultimaAtencionTipo.asStateFlow()

    override val mascotasLocal: Flow<List<MascotaEntity>> by lazy {
        database?.mascotaDao()?.getAllMascotas() ?: flowOf(emptyList())
    }

    override val consultasLocal: Flow<List<ConsultaEntity>> by lazy {
        database?.consultaDao()?.getAllConsultas() ?: flowOf(emptyList())
    }

    override val usuariosLocal: Flow<List<UsuarioEntity>> by lazy {
        database?.usuarioDao()?.getAllUsuarios() ?: flowOf(emptyList())
    }

    override val pedidosLocal: Flow<List<PedidoEntity>> by lazy {
        database?.pedidoDao()?.getAllPedidos() ?: flowOf(emptyList())
    }

    override fun init(context: Context) {
        if (isInitialized) return
        
        prefs = context.getSharedPreferences("veterinaria_prefs", Context.MODE_PRIVATE)
        database = VeterinariaDatabase.getDatabase(context)
        
        val ultimoGuardado = prefs?.getString("ULTIMO_TIPO", null)
        if (ultimoGuardado != null) {
            _ultimaAtencionTipo.value = ultimoGuardado
        }
        
        scope.launch {
            mascotasLocal.collect { lista ->
                _totalMascotasRegistradas.value = lista.size
            }
        }

        scope.launch {
            val usuariosActuales = database?.usuarioDao()?.getAllUsuarios()?.firstOrNull()
            if (usuariosActuales.isNullOrEmpty()) {
                registrarUsuario("liliana", "liliana@gmail.com", "123456")
                registrarUsuario("Colomba", "Colomba@gmail.com", "colomba123")
                registrarUsuario("Wilda", "Wilda@gmail.com", "wilda1")
            }
        }
        
        isInitialized = true
    }

    override fun registrarAtencion(nombreDueno: String, cantidadMascotas: Int, nombreMascota: String, especieMascota: String, tipoServicio: String?, edad: Int, peso: Double, consultaId: String?, fechaHora: String?, veterinario: String?, costo: Double) {
        _nombreUltimoDueno.value = nombreDueno
        _totalMascotasRegistradas.update { it + cantidadMascotas }
        _totalConsultasRealizadas.update { it + 1 }
        
        if (tipoServicio != null) {
            _ultimaAtencionTipo.value = tipoServicio
            prefs?.edit()?.putString("ULTIMO_TIPO", tipoServicio)?.apply()
        }

        val nuevaEntrada = "Mascota: $nombreMascota ($especieMascota) - Dueño: $nombreDueno"
        _listaMascotas.update { it + nuevaEntrada }
        
        scope.launch {
            agregarMascotaRoom(nombreMascota, especieMascota, edad, peso, nombreDueno)
            
            if (consultaId != null && fechaHora != null && veterinario != null) {
                val entity = ConsultaEntity(
                    idConsulta = consultaId,
                    mascotaNombre = nombreMascota,
                    duenoNombre = nombreDueno,
                    descripcion = "Atención de $tipoServicio",
                    fechaHora = fechaHora,
                    veterinario = veterinario,
                    costo = costo,
                    estado = "Pendiente"
                )
                agregarConsultaRoom(entity)
            }
        }
    }

    override suspend fun registrarUsuario(nombre: String, email: String, pass: String): UsuarioEntity {
        val entity = UsuarioEntity(nombreUsuario = nombre, email = email, pass = pass)
        database?.usuarioDao()?.insertUsuario(entity)
        return entity
    }

    override suspend fun buscarUsuario(email: String, user: String): UsuarioEntity? {
        return database?.usuarioDao()?.findByEmailOrUser(email, user)
    }

    override suspend fun agregarMascotaRoom(nombre: String, especie: String, edad: Int, peso: Double, dueno: String) {
        val nuevaMascota = MascotaEntity(
            nombre = nombre,
            especie = especie,
            edad = edad,
            pesoKg = peso,
            ultimaVacunacion = LocalDate.now(),
            nombreDueno = dueno
        )
        database?.mascotaDao()?.insertMascota(nuevaMascota)
    }

    override suspend fun agregarConsultaRoom(consulta: ConsultaEntity) {
        database?.consultaDao()?.insertConsulta(consulta)
    }

    override suspend fun registrarPedidoRoom(pedido: PedidoEntity) {
        database?.pedidoDao()?.insertPedido(pedido)
    }

    override suspend fun eliminarMascotaRoom(mascota: MascotaEntity) {
        database?.mascotaDao()?.deleteMascota(mascota)
    }

    override fun eliminarMascota(nombreMascota: String) {
        _listaMascotas.update { lista ->
            val nuevaLista = lista.filterNot { it == nombreMascota }
            if (nuevaLista.size < lista.size) {
                _totalConsultasRealizadas.update { if (it > 0) it - 1 else 0 }
                _totalMascotasRegistradas.update { if (it > 0) it - 1 else 0 }
            }
            nuevaLista
        }
    }

    override fun editarMascota(textoOriginal: String, textoNuevo: String) {
        _listaMascotas.update { lista ->
            lista.map { if (it == textoOriginal) textoNuevo else it }
        }
    }
}
