package com.brotherc.documentcenter.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EsHelper {

    @Value("${es.url}")
    private String esUrl;

    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private ObjectMapper objectMapper;

    public Mono<Void> addData(String index, Map<String, Object> data) {
        return webClientBuilder.build()
                .post()
                .uri(esUrl + "/{index}/_doc", index)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .bodyToMono(String.class)
                .then();
    }

    public Mono<Void> addData(String index, String id, Map<String, Object> data) {
        return webClientBuilder.build()
                .post()
                .uri(esUrl + "/{index}/_doc/{id}", index, id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .bodyToMono(String.class)
                .then();
    }

    @SuppressWarnings("unchecked")
    public <T> Mono<T> getById(String index, String id, Class<T> tClass) {
        return webClientBuilder.build()
                .get()
                .uri(esUrl + "/{index}/_doc/{id}", index, id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                }).map(sourceMap -> {
                    Map<String, Object> source = (Map<String, Object>) sourceMap.get("_source");
                    return objectMapper.convertValue(source, tClass);
                })
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty());
    }

    public Mono<Map<String, Object>> updateById(String index, String id, Map<String,Object> updateFields) {
        Map<String, Object> body = new HashMap<>();
        body.put("doc", updateFields);

        return webClientBuilder.build()
                .post()
                .uri(esUrl + "/{index}/_update/{id}", index, id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }

    public Mono<Void> deleteById(String index, String id) {
        return webClientBuilder.build()
                .delete()
                .uri(esUrl + "/{index}/_doc/{id}", index, id)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty());
    }

    public Mono<Void> deleteByIdList(String index, List<String> idList) {
        StringBuilder ndjson = new StringBuilder();
        for (String id : idList) {
            ndjson.append("{\"delete\":{\"_index\":\"")
                    .append(index)
                    .append("\",\"_id\":\"")
                    .append(id)
                    .append("\"}}\n");
        }

        return webClientBuilder.build()
                .post()
                .uri(esUrl + "/_bulk")
                .contentType(MediaType.parseMediaType("application/x-ndjson"))
                .bodyValue(ndjson.toString())
                .retrieve()
                .bodyToMono(Map.class)
                .then();
    }

}
