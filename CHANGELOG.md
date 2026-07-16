# Changelog — E-commerce Triê

## Fase 0 — Setup do Projeto

### Passo 0.1 — Esqueleto do projeto
- **`pom.xml`** + estrutura Maven gerada pelo Spring Initializr via `curl`
- **Função:** base do projeto Spring Boot com todas as configurações iniciais.
- **Decisões:**
  - **Spring Boot 4.1.0** (não 3.4.1 como planejado — versão não existia no Initializr)
  - **`spring-boot-starter-webmvc`** em vez de `spring-boot-starter-web` — SB 4.x separou MVC de WebFlux, e o Initializr gera `webmvc` por padrão
  - **Java alvo 21** (mesmo com `java.version=17` inicialmente, depois atualizado)
  - Dependências inclusas: JPA (Hibernate), PostgreSQL, Security, Validation, Flyway, Actuator

### Passo 0.2 — Dependências manuais no pom.xml
- **springdoc-openapi 3.0.2** — Gera o Swagger UI automaticamente. **Decisão:** versão 2.8.16 foi testada e era incompatível com SB 4.x (`WebMvcProperties` removida do pacote antigo). 3.0.2 é a primeira versão compatível.
- **sdk-java 3.3.0** — SDK oficial do Mercado Pago para criar cobranças (PIX/cartão) e processar webhooks. Versão real do Maven Central (não 2.1.24 como constava no roadmap original).
- **jjwt 0.12.6** (3 jars: api + impl + jackson) — Geração e validação de tokens JWT para autenticação stateless (Fase 7). O `impl` e `jackson` são `runtime` porque só o `api` é necessário em tempo de compilação.
- **shedlock 6.2.0** (2 jars: spring + jdbc-template) — Lock distribuído para tarefas agendadas. **Função:** garante que a expiração de reserva de estoque rode em apenas uma instância quando houver múltiplas réplicas (Fase 4).
- **logstash-logback-encoder 8.0** — Serializador JSON para logs do Logback. **Função:** produz logs estruturados consumíveis por Datadog/ELK em produção, sem alterar o código da aplicação.
- **h2** — Banco em memória. **Função:** perfil `dev` sem Docker. Scope `runtime` (não `test`) porque precisa estar disponível em execução, não só em testes.
- **testcontainers 1.20.3** (2 jars) — PostgreSQL em container para testes de integração. **Função:** os testes rodam contra um PostgreSQL real, não um H2 mockado.
- **spring-boot-starter-test** — Adicionado manualmente porque o Initializr do SB 4.x não o gerou (gera apenas os `-test` específicos como `webmvc-test`, `jpa-test`).

### Passo 0.3 — Docker Compose
- **`docker-compose.yml`** com PostgreSQL 16 Alpine, volume `pgdata` persistente e healthcheck com `pg_isready`
- **Função:** ambiente isolado e reproduzível. O healthcheck impede que `docker compose up -d` retorne antes do banco estar pronto para conexões.
- **Decisão:** Alpine Linux (~80MB em vez de ~400MB da imagem full PostgreSQL) para iniciar mais rápido.

### Passo 0.4 — application.yml
- **Função:** configuração central da aplicação. Substitui o `application.properties` gerado pelo Initializr (formato `.yml` é hierárquico e mais legível).
- **`ddl-auto: validate`** — Hibernate **nunca** cria/altera tabelas. Quem gerencia o schema é o Flyway. Se a entidade Java não bater com a tabela real, a aplicação nem inicia — evita erro silencioso em produção.
- **`open-in-view: false`** — Desliga o anti-pattern que mantém a sessão JPA aberta até o fim da requisição. Força o desenvolvedor a declarar transações explicitamente, evitando `LazyInitializationException` em lugares inesperados.
- **`jackson.default-property-inclusion: non_null`** — Remove campos nulos das respostas JSON. Substituiu `write-dates-as-timestamps` (incompatível com `tools.jackson` do SB 4.x).
- **CORS** configurado com `app.cors.allowed-origins` — preparado para Fase 9, mas já documentado.
- **`${VAR:default}`** em todos os segredos (JWT, Mercado Pago, CORS) — permite configurar sem expor valores reais no repositório.

### Passo 0.5 — Logback com JSON
- **`logback-spring.xml`** com dois perfis:
  - **`!prod`** (dev): formato texto com timestamp, nível, classe e mensagem — legível por humanos
  - **`prod`**: JSON estruturado via `LoggingEventCompositeJsonEncoder` — indexável por ferramentas de observabilidade
- **Função:** logs de DEBUG para `com.trie.ecommerce` (código da aplicação) e `org.hibernate.SQL` (queries executadas) — essencial para debugar JPA durante o desenvolvimento.

### Passo 0.6 — Subida do banco e validação
- Docker Desktop ativado com `systemctl --user start docker-desktop` (estava instalado mas parado)
- `docker compose up -d` baixou `postgres:16-alpine` (~113MB) e iniciou o container
- Aplicação conectou ao PostgreSQL via HikariCP + Flyway com sucesso
- **Criado `application-dev.yml`** como fallback para ambientes sem Docker:
  - H2 em `MODE=PostgreSQL` para compatibilidade de SQL
  - Flyway desativado, Hibernate com `ddl-auto: update` (cria as tabelas das entidades)
  - Console H2 em `/h2-console` para consultar o banco pelo navegador

### Correções técnicas aplicadas durante a fase
| Problema | Causa | Solução |
|---|---|---|
| SB 3.4.1 não existe no Initializr | Roadmap escrito antes do SB 4.x ser lançado | Initializr gerou 4.1.0 automaticamente |
| springdoc 2.x quebra com SB 4.x | Classe `WebMvcProperties` movida de pacote no SB 4 | springdoc 3.0.2 |
| `jackson.serialization.write-dates-as-timestamps` quebra bind | Jackson migrou de `com.fasterxml` para `tools.jackson` no SB 4 | Removida; substituída por `default-property-inclusion: non_null` |
| H2 não disponível em runtime | Scope `test` não coloca no classpath de execução | Alterado para `runtime` |
| Docker não rodando | Docker Desktop parado, socket não existe | Ativado com `systemctl --user start docker-desktop` |
| Tela de login do Spring Security aparecia | Security starter presente, nenhuma config liberando endpoints | `SecurityDevConfig` com perfil `dev` libera tudo |

### Pós-Fase 0 — Ajustes gerais
- **Java 17 → 21**: Alvo de compilação atualizado no `pom.xml`. **Função:** eliminar diferença entre compilação e runtime (JDK 21 instalado), liberar features da linguagem (records, pattern matching).
- **Renomeação para Triê**: Todo o projeto renomeado: pacote `com.prataelua` → `com.trie`, banco `prata_e_lua` → `trie_db`, classe principal renomeada.
- **Springdoc path**: `/swagger-ui.html` → `/swagger-ui` — compatível com a versão 3.0.2 que usa caminho diferente.

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

### Passo 2.9 — Campos de catálogo no Product
- **`Product.java`** (`entity/Product.java`): adicionados campos `imageUrl` (String), `featured` (Boolean default false), `newCollection` (Boolean default false)
- **Função:** permitir que o admin marque produtos para as seções "Mais vendidos" (featured) e "Nova coleção" (newCollection) do frontend, além de armazenar a URL da imagem principal do produto.
- **Decisão:** flags booleanas no próprio Product em vez de tabela associativa — mais simples e consultas mais rápidas com índices diretos.

### Passo 2.10 — Migration V5 — campos de catálogo
- **`V5__add_product_catalog_fields.sql`** (`resources/db/migration/V5__add_product_catalog_fields.sql`): ALTER TABLE na tabela `products` adicionando `image_url`, `featured`, `new_collection` com índices.
- **Decisão:** migration separada (V5) em vez de alterar a V1 — migrations já aplicadas nunca são modificadas. O perfil `dev` (H2 + ddl-auto: update) cria as colunas automaticamente pelas anotações JPA.

### Passo 2.11 — DTOs, Mapper e Service atualizados
- **DTOs**: `CreateProductRequest`, `UpdateProductRequest`, `ProductResponse` receberam os campos `imageUrl`, `featured`, `newCollection` (todos opcionais).
- **Mapper**: `ProductMapper.toResponse()` e `toEntity()` propagam os novos campos.
- **Service**: `ProductService.update()` faz update condicional dos novos campos.

### Passo 2.12 — Correção do bug stockQuantity
- **Problema:** `VariantResponse.stockQuantity` retornava sempre `null` — o campo `@Transient` nunca era populado.
- **Solução:** `ProductService.populateStock()` injeta `StockService` e sobrescreve cada `VariantResponse` com o estoque calculado em tempo real via `VariantResponse.withStock()`.
- **Decisão:** mantido `VariantResponse` como `record` imutável com método *wither* (`withStock`) que retorna uma nova instância com o stock populado — evita mutabilidade e efeitos colaterais.

### Passo 2.13 — Novos endpoints de catálogo
- **`ProductRepository`**: adicionados `findByFeaturedTrueAndActiveTrue()`, `findByNewCollectionTrueAndActiveTrue()`, `findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase()`, `findDistinctActiveCategories()`.
- **`ProductController`**: quatro novos endpoints:
  - `GET /api/products/featured` — produtos em destaque
  - `GET /api/products/new-collection` — nova coleção
  - `GET /api/products/search?q=texto` — busca textual
  - `GET /api/products/categories` — lista de categorias distintas
- **Correção:** adicionado `@Transactional(readOnly = true)` em todos os métodos de leitura para evitar `LazyInitializationException` (o `open-in-view: false` fechava a sessão antes do mapper acessar as variantes lazy).

### Passo 2.14 — Migration V6 — seed de produtos de exemplo
- **`V6__seed_sample_products.sql`** (`resources/db/migration/V6__seed_sample_products.sql`): insere 11 produtos com `featured`, `newCollection` e `imageUrl` populados, espelhando os dados que o frontend (`BestSellingProductsSection.tsx` e `NewCollectionHighlightSection.tsx`) espera.
- **Decisão:** migration separada em vez de data loader no código para que os dados de exemplo estejam disponíveis desde a primeira inicialização do banco, sem depender de chamada de API.
- **Nota:** as URLs de imagem são do Unsplash (placeholder). Substituir pelo CDN real quando disponível.

---

## Fase 3 — Validação de Região de Entrega

### Passo 3.1 — DeliveryZoneRepository
- **`DeliveryZoneRepository`** (`repository/DeliveryZoneRepository.java`): interface JPA com `findByCityStateAndNeighborhood()` usando `LIKE` para buscar bairro dentro de `allowedNeighborhoods`, e `findByCityAndStateAndActiveTrue()` para listar zonas disponíveis de uma cidade.
- **Decisão:** `LIKE %:neighborhood%` é simples e funciona para o volume esperado (dezenas de bairros). Se crescer, migrar para uma tabela `delivery_neighborhoods` normalizada.

### Passo 3.2 — DTOs de validação
- **`AddressValidationRequest`** (`dto/request/AddressValidationRequest.java`): record com `@NotBlank` em street, neighborhood, city, state, zipCode.
- **`AddressValidationResponse`** (`dto/response/AddressValidationResponse.java`): record com `valid`, `message`, e `availableNeighborhoods` (lista de bairros sugeridos quando inválido).

### Passo 3.3 — DeliveryZoneService
- **`DeliveryZoneService`** (`service/DeliveryZoneService.java`): lógica de validação. Se o bairro não for encontrado, busca todos os bairros disponíveis na cidade para sugerir ao usuário.
- **Decisão:** a resposta inclui a lista de bairros atendidos para o frontend exibir como sugestão, melhorando a UX sem precisar de uma segunda chamada.

### Passo 3.4 — AddressController
- **`AddressController`** (`controller/AddressController.java`): `POST /api/addresses/validate` — recebe o endereço e retorna se está dentro da área de entrega.

### Passo 3.5 — Seed de zonas de entrega
- **`V7__seed_delivery_zones.sql`** (`resources/db/migration/V7__seed_delivery_zones.sql`): insere 5 zonas de entrega em São Paulo (Zona Sul, Oeste, Norte, Leste, Centro) com seus respectivos bairros.
- **`DevDataSeeder`** (`config/DevDataSeeder.java`): `CommandLineRunner` ativo no perfil `dev` que popula as mesmas zonas no H2 (já que Flyway é desligado nesse perfil).
- **Nota:** o V2 original do roadmap foi nomeado V7 porque V5 e V6 já estavam aplicados — Flyway exige versões sequenciais.
