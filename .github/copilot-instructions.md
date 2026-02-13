# Engine for CTA - AI Coding Guidelines

## Architecture Overview

**EngineForCTA** is a Spring Boot application with dual UI paradigms: a Swing desktop GUI (`Visualizador`) and a REST/WebSocket remote control API. The system orchestrates multi-system tracing through:

- **Core Loop**: `Dispatcher` + `Receiver` threads listen on UDP sockets per system (IL, PC, ATHS, SCO), parse messages using `Algoritmos`, and apply filters
- **State Management**: `Visualizador` (main component) maintains registries: systems, queries (`Consulta`), listeners (`ModelFilter`), and active receivers via `ConcurrentHashMap`
- **Remote Protocol**: STOMP WebSocket endpoint (`/topwebsocket`) + REST controller (`EngineController`) expose actions through `RemoteEngine` service, which directly manipulates Swing UI state

## Key Patterns

### Data Model Hierarchy
- **System** → **Consulta** (query) → **Modulo** (modules) + **ModelFilter** (listeners)
- `Modulo`: name, description, mask (binary), sistema
- `Consulta`: system-scoped, contains active modules + text/model filters
- `ModelFilter`: event listener with ID, mask vector, topic broker, enabled flag

### Threading & State
- Use `ConcurrentHashMap` for thread-safe registries: `getThreadReceiverRegistry()`, `getCatalogCommandsRegistry()`
- `Receiver` implements `Runnable` + `IReceiver`; threads self-register on startup
- **Critical**: `Visualizador` is NOT thread-safe; mutations from `RemoteEngine` must trigger via `EventQueue.invokeLater()` or `SimpMessagingTemplate.convertAndSend()`

### Message Flow
1. UDP packet arrives → `Dispatcher.run()` broadcasts to all `Receiver`s for that system
2. `Receiver` parses using `Algoritmos.filterMatch()` and `splitedMatch()` (supports `&` AND logic)
3. Results published to WebSocket via `SimpMessagingTemplate` if enabled (`chckbxPublishToWebsocket`)
4. Remote clients receive on `/channel/` topics (broker configured in `WebSocketConfigRemoteEngineConfig`)

## Build & Run

**Maven**: `./mvnw spring-boot:run` (embedded Swing GUI launches, server on `:8090`)
- Rebuild on source changes: `./mvnw compile`
- Skip GUI headless: Adjust `EngineApplication.headless(true)` (breaks Visualizador)

**Data Files**: Catalog `.def` files (binary serialized) loaded at startup via `reloadCatalogos()`. CSV files (ATHS.csv, IL.csv, PC.csv, SCO.csv) in resources directory.

## Extension Points

- **New Listener Algorithm**: Extend `Algoritmos` method or add to `ModelFilter.filter()` logic
- **New Remote Command**: Add case in `EngineProtocol.recibeMensaje()` switch, call `RemoteEngine` method
- **New System**: Add `.csv` file in resources; auto-discovered via `initVectorModules()`
- **Custom Filters**: Create `ModelFilter` with new `EventMask` vector, store in `catalogoModelFilters.def`

## Anti-Patterns to Avoid

- **Direct Swing mutation from threads**: Always use `EventQueue.invokeLater()` when updating UI from `RemoteEngine` or `Receiver`
- **Blocking in `Receiver.run()`**: Keep socket reads non-blocking; avoid heavy processing in message loop
- **Mutable shared state without sync**: Use the established `ConcurrentHashMap` pattern, not bare `HashMap`
- **Binary catalog files**: `.def` files are serialized Java objects; if corrupted, app fails silently. Version and backup carefully.

## Common Tasks

| Task | Key File | Notes |
|------|----------|-------|
| Add system support | `IL.csv`, `PC.csv`, ... in resources | Loaded into `aSistemas[]` by `initVectorModules()` |
| Modify filter logic | `Algoritmos.filterMatch()` | Supports `&` AND, `\|` implicit OR |
| Add REST endpoint | `EngineController.java` | Use `@GetMapping`, inject `RemoteEngine` |
| Broadcast WebSocket msg | `EngineProtocol.enviarDirecto()` | Call with `ModelResultData` payload |
| Register new query | `Visualizador.getCatalogoConsultas().put()` then persist | Binary serialization to `.def` file |
