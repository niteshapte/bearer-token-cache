# Bearer Token Cache

In-memory caching layer for OAuth 2.0 bearer tokens, built with Spring WebFlux. Instead of requesting a fresh token from an identity provider on every outbound call, the service caches a token per request key and automatically evicts it once it expires — reducing token-endpoint traffic for downstream partner/API integrations.

## Features

1. **Reactive by design**: Built on Spring WebFlux, returning tokens as a non-blocking `Mono<BearerTokenDTO>`.
2. **Per-key caching**: Tokens are cached against a `requestId`, so different callers/partners can hold independent cached tokens.
3. **Self-expiring cache**: Each cached token schedules its own eviction based on the token's `expires_in` value — no manual cache management required by the caller.
4. **Pluggable token source**: Token retrieval is isolated behind `BearerTokenService`, so swapping the stub implementation for a real OAuth 2.0 client call is a single-class change.
5. **Lightweight footprint**: Uses Spring's built-in cache abstraction — no external cache store (Redis, Memcached) required for single-instance use.

## Overview

1. **Bearer Token Service**:
   - Responsible for producing a `BearerTokenDTO` (token type, access token, expiry).
   - Currently ships with a stub implementation — intended to be swapped for a real call to an OAuth 2.0 token endpoint.
2. **Token Cache Controller**:
   - Exposes the `/token/cache/` endpoint.
   - Caches the response using Spring's `@Cacheable`, keyed by `requestId`.
   - Schedules a one-time eviction task based on the token's `expires_in`.
3. **Cache Removal Service**:
   - Owns cache-clearing behavior, invoked once a cached token's expiry timer elapses.

## How It Works

- **Request**: A caller hits `GET /token/cache/?requestId={id}`.
- **Cache check**: If a token is already cached for that `requestId`, Spring's `@Cacheable` returns it directly without invoking the service layer.
- **Cache miss**: `BearerTokenService` is invoked to obtain a new token, which is then stored in the cache under `requestId`.
- **Expiry scheduling**: The controller reads `expires_in` from the token response and schedules a `TimerTask` to trigger cache eviction once that window elapses.
- **Eviction**: When the timer fires, `CacheRemovalService` clears the cache so the next request for that `requestId` fetches a fresh token.

## Key Technologies

- **Spring WebFlux**: Non-blocking request handling and reactive return types.
- **Spring Cache Abstraction** (`@EnableCaching`, `@Cacheable`): In-memory caching without an external store.
- **Lombok**: Reduces boilerplate on DTOs and services (`@Data`, `@RequiredArgsConstructor`).
- **Log4j2**: Structured logging via `@Log4j2`.

## Example Use Case

This service is useful anywhere a downstream API requires a bearer token but you want to avoid re-authenticating on every call. For example:

- A partner-integration layer that calls a third-party API requiring OAuth 2.0 client-credentials tokens.
- Internal microservices that need to attach a short-lived bearer token to outbound requests without each one independently managing token refresh.
- Any scenario where token-endpoint calls are rate-limited and repeated fetches need to be minimized.

## BearerTokenCacheController

The `BearerTokenCacheController` is the core of the service — it wires together token retrieval, caching, and expiry-based eviction:

```java
@Cacheable(value = "token", key = "#requestId")
@GetMapping(value = "/cache/", produces = MediaType.APPLICATION_JSON_VALUE)
public Mono<BearerTokenDTO> tokenForPartner(String requestId) {
    BearerTokenDTO tokenOutboundResponseDTO = bearerTokenService.bearerToken();
    timerOneTime = new Timer();

    Integer expiryTime = Integer.parseInt(tokenOutboundResponseDTO.getExpires_in());

    log.info("Request ID {}. Cache eviction after: {}", requestId, expiryTime);
    timerOneTime.schedule(new OneTimeEvictionTask(), expiryTime * 1000);

    return Mono.just(tokenOutboundResponseDTO);
}
```

#### Explanation

1. **Caching**: `@Cacheable(value = "token", key = "#requestId")` short-circuits the method entirely on a cache hit — `bearerTokenService.bearerToken()` is only called on a miss.
2. **Token retrieval**: On a cache miss, a token is fetched via `BearerTokenService`.
3. **Expiry-driven eviction**: The token's `expires_in` (seconds) is used to schedule a `TimerTask` that clears the cache once the token would have expired.
4. **Reactive return**: The result is wrapped in `Mono.just(...)` to fit the WebFlux reactive pipeline.

> **Note on current behavior**: eviction currently clears the *entire* cache rather than just the expired `requestId` entry — see [Known Limitations](#known-limitations) below.

## API Reference

| Method | Endpoint | Query Param | Description |
|---|---|---|---|
| `GET` | `/token/cache/` | `requestId` (string) | Returns a cached bearer token for `requestId`, fetching and caching a new one on a cache miss. |

Example:

```
GET /token/cache/?requestId=partner-123
```

Response:

```json
{
  "token_type": "Bearer",
  "expires_in": "3600",
  "access_token": "ACCESS_TOKEN"
}
```

## Getting Started

### Prerequisites

- Java 25 (or update `java.version` in `pom.xml` to match your JDK)
- Maven 3.9+

### Running the Project

1. Clone the repository:

   ```
   git clone https://github.com/niteshapte/bearer-token-cache.git
   ```

2. Navigate to the project directory:

   ```
   cd bearer-token-cache
   ```

3. Build and run:

   ```
   mvn clean package
   java -jar target/bearer-token-cache-1.0.jar
   ```

4. The service starts on port `8080` (configurable via `application.properties`):

   ```
   curl "http://localhost:8080/token/cache/?requestId=test-1"
   ```

### Configuration

| Property | Default | Description |
|---|---|---|
| `spring.application.name` | `token-cache` | Application name |
| `server.port` | `8080` | HTTP port |

## Known Limitations

This project is a working scaffold rather than a production-ready service. Before relying on it, be aware of:
- **Stubbed token source**: `BearerTokenServiceImpl` returns a hardcoded token rather than calling a real OAuth 2.0 token endpoint.
- **Per-request `Timer`**: A new `Timer` thread is created for every request; a shared scheduler or a TTL-aware cache (e.g. Caffeine) would be more efficient under load.
- **No authentication** on the caching endpoint itself.
