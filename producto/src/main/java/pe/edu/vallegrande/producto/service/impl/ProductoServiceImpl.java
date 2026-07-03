package pe.edu.vallegrande.producto.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.producto.model.Producto;
import pe.edu.vallegrande.producto.repository.ProductoRepository;
import pe.edu.vallegrande.producto.service.ProductoService;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository repository;

    @Override
    public List<Producto> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Producto> findByEstado(String estado) {
        return repository.findByEstado(estado);
    }

    @Override
    public Producto findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    @Override
    public Producto save(Producto producto) {
        return repository.save(producto);
    }

    @Override
    public Producto update(Long id, Producto producto) {
        Producto existente = findById(id);
        existente.setNombre(producto.getNombre());
        existente.setPrecio(producto.getPrecio());
        existente.setMarca(producto.getMarca());
        existente.setEstado(producto.getEstado());
        return repository.save(existente);
    }

    @Override
    public void deleteById(Long id) {
        Producto producto = findById(id);
        producto.setEstado("I");
        repository.save(producto);
    }
}
