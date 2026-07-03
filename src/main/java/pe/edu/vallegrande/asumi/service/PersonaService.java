package pe.edu.vallegrande.asumi.service;

import pe.edu.vallegrande.asumi.model.Persona;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PersonaService {

    Flux<Persona> findAll();

    Mono<Persona> findById(Long id);

    Mono<Persona> save(Persona persona);

    Mono<Persona> update(Long id, Persona persona);

    Mono<Persona> deleteLogic(Long id);

    Mono<Persona> restoreLogic(Long id);

    Mono<Void> deleteById(Long id);

}