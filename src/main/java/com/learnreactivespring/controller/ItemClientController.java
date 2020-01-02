package com.learnreactivespring.controller;

import com.learnreactivespring.domain.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class ItemClientController {

    private WebClient webClient = WebClient.create("http://localhost:8080");

    @GetMapping("/client/retrieve")
    public Flux<Item> getAllItemsUsingRetrieve() {
        return webClient.get().uri("/v1/items")
                .retrieve()
                .bodyToFlux(Item.class)
                .log("Retrieve Items from Client Project");
    }

    @GetMapping("/client/exchange")
    public Flux<Item> getAllItemsUsingExchange() {
        return webClient.get().uri("/v1/items")
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Item.class))
                .log("Exchange Items from Client Project");
    }

    @GetMapping("/client/retrieve/singleItem")
    public Mono<Item> getOneItemsUsingRetrieve() {
        String id = "ABC";
        return webClient.get().uri("/v1/items/{id}", id)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Retrieve Items from Client Project");
    }

    @GetMapping("/client/exchange/singleItem")
    public Mono<Item> getOneItemsUsingExchange() {
        String id = "ABC";
        return webClient.get().uri("/v1/items/{id}", id)
                .exchange()
                .flatMap(clientResponse -> clientResponse.bodyToMono(Item.class))
                .log("Retrieve Items from Client Project");
    }

    @PostMapping("/client/createItem")
    public Mono<Item> createItem(@RequestBody Item item) {
        return webClient.post().uri("/v1/items/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(item), Item.class)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Created item is : ");
    }

    @PutMapping("/client/updateItem/{id}")
    public Mono<Item> createItem(@PathVariable String id,
                                 @RequestBody Item item) {
        return webClient.put().uri("/v1/items/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(item), Item.class)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Updated item is : ");
    }

    @DeleteMapping("/client/deleteItem/{id}")
    public Mono<Void> createItem(@PathVariable String id) {
        return webClient.delete().uri("/v1/items/{id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .log("Updated item is : ");
    }

    @GetMapping("/client/retrieve/error")
    public Flux<Item> retrieveException() {
        return webClient.get().uri("/v1/items/runtimeException")
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> stringMono = clientResponse.bodyToMono(String.class);
                    return stringMono.flatMap(error -> {
                        log.error("Exception is : " + error);
                        throw new RuntimeException(error);
                    });
                })
                .bodyToFlux(Item.class);

    }

    @GetMapping("/client/exchange/error")
    public Flux<Item> exchangeException() {
        return webClient.get().uri("/v1/items/runtimeException")
                .exchange()
                .flatMapMany(clientResponse -> {
                    if (clientResponse.statusCode().is5xxServerError()) {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error Message is : " + errorMessage);
                                    throw new RuntimeException(errorMessage);
                                });
                    } else {
                        return clientResponse.bodyToFlux(Item.class);
                    }
                });
    }


}
