package ar.edu.utn.frc.backend.solicitudes.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.solicitudes.dto.ClienteDto;
import ar.edu.utn.frc.backend.solicitudes.entities.Cliente;
import ar.edu.utn.frc.backend.solicitudes.repositories.ClienteRepository;
import jakarta.transaction.Transactional;

@Service
public class ClienteService {
    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Guardar un nuevo cliente
    @Transactional
    public ClienteDto guardarCliente(ClienteDto clienteDto) {
        log.info("Iniciando guardarCliente con ClienteDto: {}", clienteDto);
        Cliente clienteEntity = modelMapper.map(clienteDto, Cliente.class);
        log.debug("Mapeado a entidad Cliente: {}", clienteEntity);
        Cliente clienteGuardado = clienteRepository.save(clienteEntity);
        ClienteDto resultado = modelMapper.map(clienteGuardado, ClienteDto.class);
        log.info("Cliente guardado con ID: {}", resultado.getId());
        return resultado;
    }

    // Buscar un cliente por ID
    public Optional<ClienteDto> buscarPorId(Integer id) {
        log.info("Buscando cliente por ID: {}", id);
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            log.info("Cliente encontrado con ID: {}", id);
        } else {
            log.warn("Cliente no encontrado con ID: {}", id);
        }
        return clienteOpt.map(cliente -> modelMapper.map(cliente, ClienteDto.class));
    }

    // buscar todos los clientes
    public List<ClienteDto> buscarTodos() {
        log.info("Buscando todos los clientes.");
        List<Cliente> clientes = clienteRepository.findAll();
        List<ClienteDto> resultado = clientes.stream()
                .map(cliente -> modelMapper.map(cliente, ClienteDto.class))
                .collect(Collectors.toList());
        log.info("Se encontraron {} clientes.", resultado.size());
        return resultado;
    }

    // Actualizar un cliente existente
    @Transactional
    public Optional<ClienteDto> actualizarCliente(Integer id, ClienteDto clienteDto) {
        log.info("Iniciando actualización de cliente con ID: {}", id);
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            Cliente clienteExistente = clienteOpt.get();
            log.debug("Cliente existente encontrado. Mapeando DTO: {}", clienteDto);
            modelMapper.map(clienteDto, clienteExistente);
            Cliente clienteActualizado = clienteRepository.save(clienteExistente);
            ClienteDto resultado = modelMapper.map(clienteActualizado, ClienteDto.class);
            log.info("Cliente con ID {} actualizado exitosamente.", id);
            return Optional.of(resultado);
        } else {
            log.warn("No se puede actualizar. Cliente no encontrado con ID: {}", id);
            return Optional.empty();
        }
    }

    // Eliminar un cliente por ID
    @Transactional
    public boolean eliminarCliente(Integer id) {
        log.info("Intentando eliminar cliente con ID: {}", id);
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            log.info("Cliente con ID {} eliminado exitosamente.", id);
            return true;
        }
        log.warn("No se puede eliminar. Cliente no encontrado con ID: {}", id);
        return false;
    }

    public boolean esPropietario(Integer clienteId, String sub) {
        log.info("Verificando propiedad para cliente ID: {} con sub: {}", clienteId, sub);
        // 1. Verificar si el token 'sub' es nulo o vacío
        if (sub == null || sub.isEmpty()) {
            log.warn("Verificación fallida: 'sub' (authId del token) es nulo o vacío.");
            return false;
        }

        // 2. Buscar el cliente. Si no se encuentra (Optional.isEmpty), retornar false, NO lanzar excepción.
        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        if (clienteOpt.isEmpty()) {
            log.warn("Verificación fallida: Cliente con ID {} no encontrado.", clienteId);
            return false; 
        }

        Cliente cliente = clienteOpt.get();

        // 3. Comparar el ID de autenticación del token con el ID del cliente
        boolean esDuenio = sub.equals(cliente.getAuthId());
        if (esDuenio) {
            log.info("El sub: {} coincide con el authId del cliente {}. Es Propietario.", sub, clienteId);
        } else {
            log.warn("El sub: {} NO coincide con el authId: {} del cliente {}. No es Propietario.", sub, cliente.getAuthId(), clienteId);
        }
        return esDuenio;
    }

}