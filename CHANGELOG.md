# Changelog — E-commerce Triê

## Fase 0 — Setup do Projeto

### Passo 0.1 — Esqueleto do projeto
- Gerado pelo Spring Initializr via `curl`
- **Spring Boot 4.1.0** (não 3.4.1 — versão indisponível no Initializr)
- **Java 21**, Maven, PostgreSQL, JPA, Security, Validation, Flyway, Actuator
- **Pacote base:** `com.trie.ecommerce` (anteriormente `com.prataelua`)
- Starter `spring-boot-starter-webmvc` (SB 4.x usa `webmvc` no lugar de `web`)

### Passo 0.2 — Dependências manuais no pom.xml
- **springdoc-openapi 3.0.2** — Swagger UI (2.8.16 era incompatível com SB 4.x)
- **sdk-java 3.3.0** — Mercado Pago SDK (versão real no Maven Central, não 2.1.24)
- **jjwt 0.12.6** — Geração de tokens JWT (api + impl + jackson)
- **shedlock 6.2.0** — Lock entre instâncias no @Scheduled
- **logstash-logback-encoder 8.0** — Logs em JSON para produção
- **h2** — Banco em memória para desenvolvimento (scope `runtime`)
- **testcontainers 1.20.3** — PostgreSQL em container para testes
- **spring-boot-starter-test** + **security-test**
- O Initializr não gerou `spring-boot-starter-test` básico — adicionado manualmente

### Passo 0.3 — Docker Compose
- PostgreSQL 16 Alpine com volume persistente e healthcheck

### Passo 0.4 — application.yml
- Convertido de `.properties` para `.yml` (hierarquia mais legível)
- `ddl-auto: validate` — Flyway gerencia o schema, Hibernate só valida
- `open-in-view: false` — desliga anti-pattern de sessão JPA aberta até a view
- Jackson serialization → removido `write-dates-as-timestamps` (incompatível com `tools.jackson` do SB 4.x), substituído por `default-property-inclusion: non_null`
- CORS configurado para localhost:3000 e localhost:5173

### Passo 0.5 — Logback com JSON
- Perfil `!prod`: logs em texto legível com timestamp, nível, classe
- Perfil `prod`: logs em JSON estruturado (Datadog/ELK)
- Loggers em DEBUG para `com.prataelua.ecommerce` e `org.hibernate.SQL`

### Passo 0.6 — Subida do banco e validação
- Docker Desktop ativado via `systemctl --user start docker-desktop`
- `docker compose up -d` → PostgreSQL 16 rodando e saudável
- Aplicação conectou ao PostgreSQL via HikariCP + Flyway
- Criado perfil `application-dev.yml` como fallback:
  - H2 em modo compatibilidade PostgreSQL
  - Flyway desativado, `ddl-auto: update` para desenvolvimento sem Docker
  - Console H2 em `/h2-console`

### Correções técnicas aplicadas durante a fase
| Problema | Solução |
|---|---|
| SB 3.4.1 inexistente no Initializr | Usar SB 4.1.0 (gerado automaticamente) |
| springdoc 2.x quebra com SB 4.x | springdoc 3.0.2 |
| `jackson.serialization.write-dates-as-timestamps` quebra bind | Removido |
| H2 em escopo `test` não disponível em runtime | Alterado para `runtime` |
| Docker não rodando | Ativado Docker Desktop |

### Notas de desenvolvimento
- **SecurityDevConfig**: Configuração temporária que libera todos os endpoints no perfil `dev`. Remover quando a Fase 7 (Autenticação JWT) for implementada.

### Pós-Fase 0 — Ajustes gerais
- **Java 17 → 21**: Alvo de compilação atualizado (`pom.xml`, roadmap, system prompt)
- **Spring Boot 3 → 4.1**: Corrigido no system prompt do opencode.json
- **Renomeação para Triê**: Pacote `com.prataelua` → `com.trie`, banco `prata_e_lua` → `trie_db`, class `EcommercePrataLuaApplication` → `EcommerceTrieApplication`
- **Springdoc path**: `/swagger-ui.html` → `/swagger-ui` (compatível com Springdoc 3.0.2)

## Fase 1 — Modelagem de Dados

### Passo 1.1 — Estrutura de pacotes
- Criada estrutura de diretórios em `src/main/java/com/trie/ecommerce/`:
  `config/`, `controller/`, `dto/request/`, `dto/response/`, `entity/`, `enums/`, `exception/`, `mapper/`, `repository/`, `security/`, `service/`

### Passo 1.2 — Enums
- `PricingType.java`: `FIXED`, `BY_GRAM`
- `StockMovementType.java`: `IN`, `OUT`, `RESERVE`, `RELEASE`
- `OrderStatus.java`: `DRAFT → PENDING → PAID → PREPARING → OUT_FOR_DELIVERY → DELIVERED → CANCELLED`
- `PaymentStatus.java`: `PENDING`, `APPROVED`, `REJECTED`, `REFUNDED`
- `UserRole.java`: `CUSTOMER`, `ADMIN`

### Passo 1.3 — Entidades JPA
- **Product**: nome, descrição, categoria, material, pricingType, pricePerGram, active, timestamps, @OneToMany variants
- **ProductVariant**: product, size, weightInGrams, price, sku, isUniquePiece, @Transient stockQuantity, @Version, active
- **StockMovement**: variant, type (IN/OUT/RESERVE/RELEASE), quantity, reason, orderReference, createdAt
- **Customer**: name, email, password, phone, role, @OneToMany addresses, createdAt
- **Address**: customer, street, number, complement, neighborhood, city, state, zipCode, isDefault
- **DeliveryZone**: name, city, state, allowedNeighborhoods, active
- **Order**: customer, deliveryAddress, status (DRAFT padrão), totalAmount, reservedUntil, timestamps, @OneToMany items/statusHistory, @OneToOne payment
- **OrderItem**: order, variant, quantity, **priceAtPurchase** (preço congelado)
- **Payment**: order, mercadopagoPaymentId, status, method, amount, qrCode/qrCodeBase64, timestamps
- **OrderStatusHistory**: order, fromStatus, toStatus, changedAt, notes

### Dependências adicionadas
- **lombok**: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` em todas as entidades (reduz boilerplate)

### Passo 1.13 — Migration Flyway V1
- Criado `V1__create_tables.sql` com todas as tabelas, FKs, índices e constraints:
  `products`, `product_variants`, `stock_movements`, `customers`, `addresses`,
  `delivery_zones`, `orders`, `order_items`, `payments`, `order_status_history`
- Nomenclatura: snake_case, plural, índices em colunas mais consultadas
- Relacionamentos com ON DELETE CASCADE/SET NULL adequados
- Só executado no perfil padrão (PostgreSQL). Perfil `dev` usa Hibernate ddl-auto.

## Fase 2 — Estoque e Produtos (CRUD) + Tratamento de Erro Global

### Passo 2.1 — Repositories
- **`ProductRepository`** (`repository/ProductRepository.java`)
  - Função: interface de acesso ao banco para produtos. `findByActiveTrue()` filtra apenas ativos (soft delete), `findByCategory()` para filtro no catálogo.
- **`ProductVariantRepository`** (`repository/ProductVariantRepository.java`)
  - Função: acesso a variantes de produto. `findBySku()` garante unicidade de SKU, `existsBySku()` evita duplicatas.
  - `findByIdWithLock()` usa `@Lock(PESSIMISTIC_WRITE)` — emite `SELECT ... FOR UPDATE` no PostgreSQL. **Essencial para Fase 4**: impede que duas threads reservem a mesma peça única simultaneamente.
- **`StockMovementRepository`** (`repository/StockMovementRepository.java`)
  - Função: persistência de todas as movimentações de estoque.
  - `calculateCurrentStock()` é uma **JPQL com CASE** que soma entradas (IN, RELEASE) e subtrai saídas (OUT, RESERVE). **Decisão crítica:** o saldo nunca vem de um campo fixo, é sempre calculado do histórico — isso garante auditabilidade total de como cada unidade entrou/saiu.

### Passo 2.2 — DTOs de Request
- **`CreateProductRequest`** (`dto/request/CreateProductRequest.java`)
  - Função: define o contrato da API para criar um produto. Usa Java `record` para imutabilidade e Bean Validation nos campos obrigatórios.
  - `variants @NotEmpty` — obriga enviar ao menos uma variante na criação (produto sem variante não faz sentido).
- **`CreateVariantRequest`** (`dto/request/CreateVariantRequest.java`)
  - Função: dados de cada variante dentro do produto. `weightInGrams` e `price` com `@Positive` impedem valores negativos ou zero.
- **`StockMovementRequest`** (`dto/request/StockMovementRequest.java`)
  - Função: entrada/saída de estoque. `variantId` e `type` são obrigatórios para saber onde e o que aconteceu.

### Passo 2.3 — DTOs de Response
- **`ProductResponse`** + **`VariantResponse`** (`dto/response/`)
  - Função: formato de saída da API. **Nunca expõe a entidade JPA diretamente** — desacopla o contrato público da estrutura interna do banco.
  - `VariantResponse` inclui `stockQuantity` (calculado em tempo real), mas **não expõe** campos internos como `version` (lock) ou `active`.
- **`UpdateProductRequest`** (`dto/request/UpdateProductRequest.java`)
  - Função: atualização parcial (PATCH). Todos os campos são opcionais — se o cliente enviar só o `name`, apenas o nome é alterado.

### Passo 2.4 — Mappers
- **`ProductMapper`** (`mapper/ProductMapper.java`)
  - Função: centraliza a conversão entre entidades JPA e DTOs. `toResponse()` com null-safe em listas de variantes.
  - **Decisão:** `pricePerGram` só é preenchido quando `pricingType == BY_GRAM`. Se for `FIXED`, fica nulo — evita campo irrelevante na resposta.

### Passo 2.5 — Services
- **`ProductService`** (`service/ProductService.java`)
  - Função: regras de negócio do produto. `create()` gera SKU automático (UUID de 8 chars) se não informado. `delete()` faz soft delete (active=false) em vez de remover — preserva histórico de pedidos.
  - **Decisão:** a criação de variantes é em cascata (Product gerencia o ciclo de vida das variantes), mas a validação de unicidade de SKU fica no banco (unique index), não no service.
- **`StockService`** (`service/StockService.java`)
  - Função: centro de controle do estoque. `registerMovement()` valida se há saldo suficiente ANTES de registrar uma saída. `calculateCurrentStock()` soma o histórico.
  - **Decisão:** usa `StockMovementType` nos cálculos — se no futuro adicionarmos um novo tipo, basta ajustar o CASE.
- **Exceções customizadas** (`exception/`)
  - `ResourceNotFoundException` (404): produto/variante/pedido não encontrado
  - `InsufficientStockException` (409): tentativa de vender mais do que o estoque permite
  - `InvalidStatusTransitionException` (422): pulo de etapas no fluxo de pedido (preparada para Fase 6)
  - `BusinessException` (422): classe base para erros de negócio genéricos

### Passo 2.6 — GlobalExceptionHandler
- **`GlobalExceptionHandler`** (`exception/GlobalExceptionHandler.java`)
  - Função: **único ponto** que traduz exceções Java em respostas HTTP com formato consistente. Sem ele, o Spring retornaria stack traces ou HTML padrão do Tomcat.
  - Handlers implementados: `ResourceNotFoundException` → 404, `MethodArgumentNotValidException` → 400 (com campo:mensagem), `OptimisticLockException` → 409, `PessimisticLockingFailureException` → 409, `InsufficientStockException` → 409, `InvalidStatusTransitionException` → 422, `BusinessException` → 422, `Exception` genérica → 500 (com log do erro).
- **`ErrorResponse`** (`exception/ErrorResponse.java`)
  - Função: formato padronizado de erro. Todo erro retorna `{"code": "NOT_FOUND", "message": "...", "timestamp": "..."}` — o frontend sempre sabe como interpretar.

### Passo 2.7 — Controllers
- **`ProductController`** (`controller/ProductController.java`)
  - Função: endpoints REST para gestão de produtos. `POST /api/products` cria com variantes, `DELETE /api/products/{id}` desativa (soft delete).
  - **Decisão:** usa `@Valid` para ativar Bean Validation e `@ResponseStatus(HttpStatus.NO_CONTENT)` no DELETE para retornar 204 sem corpo.
- **`StockController`** (`controller/StockController.java`)
  - Função: movimentação de estoque por produto. `POST /{productId}/stock/movements` registra entrada/saída, `GET /{productId}/stock` retorna saldo.
  - `@Tag` + `@Operation` + `@ApiResponse` documentam cada endpoint no Swagger.
- **Anotações Swagger adicionadas** para que o Swagger UI (em `/swagger-ui`) exiba descrições legíveis e códigos de resposta esperados.
