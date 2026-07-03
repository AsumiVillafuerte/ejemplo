package pe.edu.vallegrande.asumi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.asumi.model.Persona;
import pe.edu.vallegrande.asumi.repository.PersonaRepository;
import pe.edu.vallegrande.asumi.service.PersonaService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository repository;

    @Override
    public Flux<Persona> findAll() {
        return repository.findAll();
    }

    @Override
    public Mono<Persona> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Mono<Persona> save(Persona persona) {
        persona.setEstado("ACTIVO");
        return repository.save(persona);
    }

    @Override
    public Mono<Persona> update(Long id, Persona persona) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setNombre(persona.getNombre());
                    existing.setApellido(persona.getApellido());
                    existing.setEmail(persona.getEmail());
                    return repository.save(existing);
                });
    }

    @Override
    public Mono<Persona> deleteLogic(Long id) {
        return repository.findById(id)
                .flatMap(persona -> {
                    persona.setEstado("INACTIVO");
                    return repository.save(persona);
                });
    }

    @Override
    public Mono<Persona> restoreLogic(Long id) {
        return repository.findById(id)
                .flatMap(persona -> {
                    persona.setEstado("ACTIVO");
                    return repository.save(persona);
                });
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return repository.deleteById(id);
    }
}