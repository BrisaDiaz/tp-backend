package ar.edu.utn.frc.backend.recursos.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.recursos.dto.CargoGestionDto;
import ar.edu.utn.frc.backend.recursos.dto.PrecioCombustibleDto;
import ar.edu.utn.frc.backend.recursos.services.CargoGestionService;
import ar.edu.utn.frc.backend.recursos.services.PrecioCombustibleService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tarifas")
public class TarifaController {

    @Autowired
    private PrecioCombustibleService combustibleService;
    @Autowired
    private CargoGestionService cargoGestionService;

    // Crea un nuevo precio de combustible y finaliza la vigencia del anterior
    @PostMapping("/combustible")
    public ResponseEntity<PrecioCombustibleDto> setPrecioCombustible(@Valid @RequestBody PrecioCombustibleDto dto) {
        PrecioCombustibleDto newPrice = combustibleService.guardarNuevoPrecio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPrice);
    }

    // Obtiene el precio vigente de combustible
    @GetMapping("/combustible")
    public ResponseEntity<PrecioCombustibleDto> getPrecioCombustibleActual() {
        return combustibleService.buscarPrecioVigente()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crea un nuevo cargo y finaliza la vigencia del anterior
    @PostMapping("/gestion")
    public ResponseEntity<CargoGestionDto> setCargoGestion(@Valid @RequestBody CargoGestionDto dto) {
        CargoGestionDto newCargo = cargoGestionService.guardarNuevoCargo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCargo);
    }

    // Obtiene el cargo vigente
    @GetMapping("/gestion")
    public ResponseEntity<CargoGestionDto> getCargoGestionActual() {
        return cargoGestionService.buscarCargoVigente()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}