package pe.edu.vallegrande.producto.service;

import pe.edu.vallegrande.producto.model.Producto;
import java.util.List;

public interface ProductoService {

    List<Producto> findAll();

    List<Producto> findByEstado(String estado);

    Producto findById(Long id);

    Producto save(Producto producto);

    Producto update(Long id, Producto producto);

    void deleteById(Long id);
}
