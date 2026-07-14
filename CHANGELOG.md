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
- Criada hierarquia de diretórios em `src/main/java/com/trie/ecommerce/` para separar responsabilidades:
  `config/`, `controller/`, `dto/request/`, `dto/response/`, `entity/`, `enums/`, `exception/`, `mapper/`, `repository/`, `security/`, `service/`
- **Função:** organizar o código por camada (MVC), facilitando navegação e manutenção.
- **Decisão:** pacotes separados para `dto/request` e `dto/response` em vez de um único `dto/` — evita misturar classes de entrada e saída da API.

### Passo 1.2 — Enums
- **`PricingType`** (`enums/PricingType.java`): `FIXED`, `BY_GRAM`
  - Função: define como o preço do produto é calculado. `BY_GRAM` permite precificar por peso (comum em joias de prata).
- **`StockMovementType`** (`enums/StockMovementType.java`): `IN`, `OUT`, `RESERVE`, `RELEASE`
  - Função: classifica cada movimentação de estoque. `RESERVE` separa itens no carrinho, `RELEASE` devolve ao expirar a reserva.
- **`OrderStatus`** (`enums/OrderStatus.java`): `DRAFT → PENDING → PAID → PREPARING → OUT_FOR_DELIVERY → DELIVERED → CANCELLED`
  - Função: máquina de estados do pedido. `DRAFT` funciona como carrinho ativo.
- **`PaymentStatus`** (`enums/PaymentStatus.java`): `PENDING`, `APPROVED`, `REJECTED`, `REFUNDED`
  - Função: espelha os status retornados pelo Mercado Pago no webhook.
- **`UserRole`** (`enums/UserRole.java`): `CUSTOMER`, `ADMIN`
  - Função: diferencia cliente comum de administrador para controle de acesso (Fase 7).
- **Decisão:** todos usam `@Enumerated(EnumType.STRING)` — se a ordem dos valores mudar no código, os dados salvos no banco não quebram.

### Passo 1.3 a 1.12 — Entidades JPA
- **`Product`** (`entity/Product.java`)
  - Função: representa um produto do catálogo (anel, colar, pulseira). `pricingType` decide se o preço é fixo ou por grama. `@OneToMany variants` carrega as variações (tamanhos).
  - **Decisão:** `@PrePersist` e `@PreUpdate` para timestamps automáticos — evita esquecer de setar createdAt/updatedAt na mão.
- **`ProductVariant`** (`entity/ProductVariant.java`)
  - Função: variação concreta do produto (ex: anel tamanho 18, 5g). `isUniquePiece` marca peças únicas (estoque máximo 1). `@Version` prepara lock otimista para concorrência (Fase 4).
  - **Decisão:** `stockQuantity` é `@Transient` — nunca persistido, sempre calculado do histórico de movimentos.
- **`StockMovement`** (`entity/StockMovement.java`)
  - Função: registro imutável de cada movimentação. `orderReference` vincula a movimentação ao pedido que a causou.
  - **Decisão:** estoque é **100% auditável** — nunca se perde o histórico de como cada unidade entrou/saiu.
- **`Customer`** (`entity/Customer.java`)
  - Função: dados do cliente. `role` default `CUSTOMER`. `@OneToMany addresses` permite múltiplos endereços cadastrados.
- **`Address`** (`entity/Address.java`)
  - Função: endereço vinculado a um cliente. `isDefault` marca o endereço principal para entrega.
- **`DeliveryZone`** (`entity/DeliveryZone.java`)
  - Função: define áreas onde a entrega é feita. `allowedNeighborhoods` como texto delimitado por vírgulas — simples de consultar com `LIKE`.
  - **Decisão:** modelo de lista fixa de bairros em vez de raio geográfico. Mais simples e atende o caso real (você mesmo entrega).
- **`Order`** (`entity/Order.java`)
  - Função: pedido/carrinho. `status = DRAFT` significa carrinho ativo. `reservedUntil` controla expiração da reserva de estoque. `totalAmount` é calculado e armazenado.
  - **Decisão:** `Order` acumula múltiplas responsabilidades (carrinho, pedido, histórico) porque no fluxo real são o mesmo objeto em estados diferentes.
- **`OrderItem`** (`entity/OrderItem.java`)
  - Função: item dentro do pedido. `priceAtPurchase` congela o preço no momento da compra.
  - **Decisão crítica:** o preço nunca é puxado do `ProductVariant.price` atual — se o admin alterar o preço depois, pedidos antigos não são afetados.
- **`Payment`** (`entity/Payment.java`)
  - Função: representa a cobrança no Mercado Pago. `mercadopagoPaymentId` vincula ao ID externo. `qrCode` e `qrCodeBase64` armazenam o PIX gerado.
- **`OrderStatusHistory`** (`entity/OrderStatusHistory.java`)
  - Função: auditoria de cada transição de status. Permite ao cliente acompanhar "linha do tempo" do pedido e ao admin ver o histórico completo.
- **Lombok**: `@Getter/@Setter/@NoArgsConstructor/@AllArgsConstructor/@Builder` adicionados para reduzir boilerplate. **Decisão:** Lombok gera os métodos em tempo de compilação, eliminando centenas de linhas de getters/setters manuais sem impacto em runtime.

### Passo 1.13 — Migration Flyway V1
- **`V1__create_tables.sql`** (`resources/db/migration/V1__create_tables.sql`)
  - Função: versão 1 do schema do banco. Toda vez que o schema mudar, uma nova migration (V2, V3...) é criada — nunca se altera uma migration já aplicada.
  - **Decisões:**
    - `BIGSERIAL` para IDs (auto-incremento PostgreSQL)
    - `NUMERIC(10,2)` para valores monetários (nunca `double`/`float`)
    - `NUMERIC(10,4)` para peso (4 casas decimais para precisão de gramas)
    - `VARCHAR(20)` para enums (STRING) — se o enum crescer, aumenta o tamanho na migration seguinte
    - Foreign keys com `ON DELETE CASCADE` em relações de pertencimento (ex: deletar produto → deleta variantes)
    - `ON DELETE SET NULL` em delivery_address — se um endereço for removido, o pedido mantém-se com endereço nulo
    - Índices em colunas de busca frequente (sku, email, status, customer_id, variant_id)
  - **Importante:** migrations só rodam no perfil padrão (PostgreSQL). O perfil `dev` usa Hibernate `ddl-auto: update` com H2, ignorando Flyway.

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
