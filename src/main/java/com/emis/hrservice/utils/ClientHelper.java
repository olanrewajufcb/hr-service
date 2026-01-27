package com.emis.hrservice.utils;

import java.net.ConnectException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.emis.hrservice.config.ServiceConfigurationProperties;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@RequiredArgsConstructor
@Slf4j
@Component
public class ClientHelper {

    private final WebClient webClient;
    private final ServiceConfigurationProperties properties;

    public <R> Mono<R> getRequestWithPathVariables(String url,
                                                   Map<String, ?> pathVariables,
                                                   MultiValueMap<String, String> headers,
                                                   Class<R> responseType){
        Map<String, String> safePath = pathVariables.entrySet().stream()
                        .filter(e -> e.getValue() != null)
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                        e -> String.valueOf(e.getValue()),
        (a, b) -> b, LinkedHashMap::new));

        log.info("Making request to: {} with path vars: {}", url, safePath);

        return webClient
                .get()
                .uri(url, safePath)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .bodyToMono(responseType);

    }
    public static MultiValueMap<String, String> getHeaders(){
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(1);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    public <R> Mono<R> getRequestWithParameters(
            String url,
            Map<String, ?> pathVariables,
            Map<String, ?> queryParams,
            MultiValueMap<String, String> headers,
            Class<R> responseType) {

        Map<String, String> safePath = pathVariables != null
                ? pathVariables.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.valueOf(e.getValue()),
                        (a, b) -> b,
                        LinkedHashMap::new))
                : new LinkedHashMap<>();

        log.info("Making GET request to: {} with path vars: {}, query params: {}",
                url, safePath, queryParams);

    return webClient
        .get()
        .uri(
            uriBuilder -> {
              var builder = uriBuilder.path(url);

              if (queryParams != null) {
                queryParams.forEach(
                    (key, value) -> {
                      if (value != null) {
                        builder.queryParam(key, value);
                      }
                    });
              }

              return builder.build(safePath);
            })
        .headers(
            httpHeaders -> {
              if (headers != null && !headers.isEmpty()) {
                httpHeaders.addAll(headers);
              }
            })
        .retrieve()
        .bodyToMono(responseType)
        .timeout(Duration.ofSeconds(properties.getTimeout()))
        .retryWhen(
            Retry.backoff(3, Duration.ofMillis(properties.getTimeout()))
                    .filter(this::isRetryable)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure()));
    }
    private boolean isRetryable(Throwable throwable) {
        return throwable instanceof WebClientResponseException ex
                && (ex.getStatusCode().is5xxServerError()
                || ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS
                || ex.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE
                || ex.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT)
                || throwable instanceof ConnectException
                || throwable instanceof TimeoutException
                || throwable instanceof ReadTimeoutException;
    }

  public <R> Flux<R> getRequestWithPathVariablesFlux(
      String url,
      Map<String, ?> pathVariables,
      MultiValueMap<String, String> headers,
      Class<R> responseType) {
        Map<String, String> safePath = pathVariables.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> String.valueOf(e.getValue()),
                        (a, b) -> b, LinkedHashMap::new));

        log.info("Making request to: {} with path vars: {}", url, safePath);

        return webClient
                .get()
                .uri(url, safePath)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .bodyToFlux(responseType);

    }

    public <T> Flux<T> post(String url, Object body, MultiValueMap<String, String> headers, Class<T> responseType) {

        return webClient.post()
                .uri(url)
                .headers(h -> h.addAll(headers))
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(responseType);
    }


    public <T, R> Mono<R> postRequest(String url, T requestBody, Map<String, ?> pathVariables,
                                      MultiValueMap<String, String> headers, Class<R> responseType) {

        Map<String, String> safePath = pathVariables != null
                ? pathVariables.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.valueOf(e.getValue()),
                        (a, b) -> b,
                        LinkedHashMap::new))
                : new LinkedHashMap<>();

        log.info("Making POST request to: {} with path vars: {}", url, safePath);

        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path(url).build(safePath))
                .headers(httpHeaders -> {
                    if (headers != null && !headers.isEmpty()) {
                        httpHeaders.addAll(headers);
                    }
                })
                .retrieve()
                .bodyToMono(responseType);
    }

    public <T, R> Mono<R> putRequest(String url, T requestBody, Map<String, ?> pathVariables,
                                        MultiValueMap<String, String> headers, Class<R> responseType) {

        Map<String, String> safePath = pathVariables != null
                ? pathVariables.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.valueOf(e.getValue()),
                        (a, b) -> b,
                        LinkedHashMap::new))
                : new LinkedHashMap<>();

        log.info("Making PUT request to: {} with path vars: {}", url, safePath);

        return webClient
                .put()
                .uri(uriBuilder -> uriBuilder.path(url).build(safePath))
                .headers(httpHeaders -> {
                    if (headers != null && !headers.isEmpty()) {
                        httpHeaders.addAll(headers);
                    }
                })
                .retrieve()
                .bodyToMono(responseType);
            }


}
