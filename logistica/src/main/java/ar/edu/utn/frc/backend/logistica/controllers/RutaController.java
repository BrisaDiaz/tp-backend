package ar.edu.utn.frc.backend.logistica.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/rutas")
public class RutaController {

    @GetMapping("/{solicitudId}/tentativas")
    public void obtenerTentativas() {
        // 1. Obtener la solicitud por ID  desde el servicio de solicitudes

        // 2. Extraer los depósitos de origen y destino de la solicitud

        // 3. Obtener todos los depósitos desde el servicio de recursos

        // 4. Filtrar los depósitos de origen y destino

        // 5. Construir la lista de puntos intermedios entre origen y destino

        // 6. Generar las rutas tentativas utilizando un algoritmo de rutas

        // 7. Calcular la distancia y duración de cada tramo de las rutas através del servicio de mapas

        // 8. Obtener el costo del combustible y el cargo por gestión desde el servicio de recuros

        // 9. Calcular el costo total de cada ruta tentativa

        // 10. Devolver la lista de rutas tentativas con sus detalles
    }
    
}
