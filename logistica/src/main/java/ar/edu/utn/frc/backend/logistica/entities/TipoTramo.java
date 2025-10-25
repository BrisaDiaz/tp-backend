package ar.edu.utn.frc.backend.logistica.entities;

public enum TipoTramo {
    ORIGEN_DEPOSITO, // Tramo que va desde el depósito de origen a un depósito intermedio.
    DEPOSITO_DEPOSITO, // Tramo entre dos depósitos intermedios.
    DEPOSITO_DESTINO, // Tramo que va desde el último depósito intermedio el depósito de destino.
    ORIGEN_DESTINO // Tramo que va desde el depósito de origen al depósito de destino (ruta de un solo tramo).
}
