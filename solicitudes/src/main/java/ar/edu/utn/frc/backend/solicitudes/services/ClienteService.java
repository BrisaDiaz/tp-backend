package ar.edu.utn.frc.backend.solicitudes.services;

import ar.edu.utn.frc.backend.solicitudes.repositories.ClienteRepository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.solicitudes.dto.ClienteDto;
import ar.edu.utn.frc.backend.solicitudes.entities.Cliente;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Guardar un nuevo cliente
    @Transactional
    public ClienteDto guardarCliente(ClienteDto clienteDto) {
        Cliente clienteEntity = modelMapper.map(clienteDto, Cliente.class);
        Cliente clienteGuardado = clienteRepository.save(clienteEntity);
        return modelMapper.map(clienteGuardado, ClienteDto.class);
    }

    // Buscar un cliente por ID
    public Optional<ClienteDto> buscarPorId(Integer id) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        return clienteOpt.map(cliente -> modelMapper.map(cliente, ClienteDto.class));
    }

    // buscar todos los clientes
    public List<ClienteDto> buscarTodos() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clientes.stream()
                .map(cliente -> modelMapper.map(cliente, ClienteDto.class))
                .collect(Collectors.toList());
    }

    // Actualizar un cliente existente
    @Transactional
    public Optional<ClienteDto> actualizarCliente(Integer id, ClienteDto clienteDto) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            Cliente clienteExistente = clienteOpt.get();
            modelMapper.map(clienteDto, clienteExistente);
            Cliente clienteActualizado = clienteRepository.save(clienteExistente);
            return Optional.of(modelMapper.map(clienteActualizado, ClienteDto.class));
        } else {
            return Optional.empty();
        }
    }

    // Eliminar un cliente por ID
    @Transactional
    public boolean eliminarCliente(Integer id) {
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
