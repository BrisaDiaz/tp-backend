package ar.edu.utn.frc.backend.recursos.services;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.recursos.dto.CiudadDto;
import ar.edu.utn.frc.backend.recursos.entities.Ciudad;
import ar.edu.utn.frc.backend.recursos.repositories.CiudadRepository;
import jakarta.transaction.Transactional;

@Service    
public class CiudadService {
    @Autowired
    private CiudadRepository ciudadRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Dar de alta una  ciudad
    @Transactional
    public CiudadDto guardarCiudad(CiudadDto CiudadDto) {
        Ciudad ciudad = modelMapper.map(CiudadDto, Ciudad.class);
        Ciudad CiudadGuardado = ciudadRepository.save(ciudad);
        return modelMapper.map(CiudadGuardado, CiudadDto.class);
    }

    // Buscar ciudad por código postal
    public Optional<CiudadDto> buscarPorCodigoPostal(String codigoPostal) {
        return ciudadRepository.findByCodigoPostal(codigoPostal)
                .map(ciudad -> modelMapper.map(ciudad, CiudadDto.class));
    }

    // Buscar ciudad por ID
    public Optional<CiudadDto> buscarPorId(Integer id) {
        return ciudadRepository.findById(id)
                .map(ciudad -> modelMapper.map(ciudad, CiudadDto.class));
    }

    // Actualizar ciudad existente
    @Transactional
    public Optional<CiudadDto> actualizarCiudad(Integer id, CiudadDto ciudadDto) {
        Optional<Ciudad> ciudadOpt = ciudadRepository.findById(id);
        if (ciudadOpt.isPresent()) {
            Ciudad ciudadExistente = ciudadOpt.get();
            modelMapper.map(ciudadDto, ciudadExistente);
            Ciudad ciudadActualizado = ciudadRepository.save(ciudadExistente);
            return Optional.of(modelMapper.map(ciudadActualizado, CiudadDto.class));
        } else {
            return Optional.empty();
        }
    }

}
