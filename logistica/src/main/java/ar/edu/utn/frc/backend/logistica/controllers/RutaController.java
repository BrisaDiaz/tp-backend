package ar.edu.utn.frc.backend.logistica.controllers;

import ar.edu.utn.frc.backend.logistica.services.RutaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/rutas")
public class RutaController {
    @Autowired
    private RutaService rutaService;

    @GetMapping("/{solicitudId}/tentativas")
    public void obtenerTentativas(@PathVariable Integer solicitudId) {

    }

}
