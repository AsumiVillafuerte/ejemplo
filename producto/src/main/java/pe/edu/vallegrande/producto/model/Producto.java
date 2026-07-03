package pe.edu.vallegrande.producto.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String estado;

    @Column(name = "fecha_hora_crear", nullable = false)
    private LocalDateTime fechaHoraCrear;

    @PrePersist
    protected void onCreate() {
        fechaHoraCrear = LocalDateTime.now();
        if (estado == null) {
            estado = "A";
        }
    }
}
