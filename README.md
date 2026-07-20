# Triê E-commerce

E-commerce de joias de prata com backend em Java/Spring Boot e frontend em React/TSX.

## Stack

- **Java 21** + **Spring Boot 4.x**
- **PostgreSQL 16** (via Docker)
- **Flyway** (migrações de banco)
- **Spring Security + JWT** (autenticação stateless)
- **Springdoc OpenAPI 3** (documentação da API)
- **ShedLock** (lock distribuído para tarefas agendadas)
- **Lombok** (redução de boilerplate)

## Pré-requisitos

- Java 21+
- Docker Desktop (para PostgreSQL)

## Como rodar

```bash
# 1. Subir o banco
docker compose up -d

# 2. Iniciar a aplicação (perfil dev com H2, sem Docker)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Ou com PostgreSQL (perfil default)
./mvnw spring-boot:run
```

## Variáveis de ambiente

| Variável | Default | Descrição |
|---|---|---|
| `DB_USER` | `postgres` | Usuário do PostgreSQL |
| `DB_PASSWORD` | `postgres` | Senha do PostgreSQL |
| `JWT_SECRET` | `chave-secreta-...` | Chave para assinatura JWT (min 64 chars) |
| `WHATSAPP_NUMBER` | `5511999999999` | Número para link do WhatsApp |
| `CORS_ORIGINS` | `http://localhost:3000,...` | Origens permitidas pelo CORS |

## Documentação da API

Com a aplicação rodando, acesse:

- **Swagger UI:** http://localhost:8080/swagger-ui
- **OpenAPI JSON:** http://localhost:8080/api-docs

## Fluxo de uso (exemplo)

```bash
# 1. Criar admin (ou usar o seed: admin@trie.com / admin123)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@trie.com","password":"admin123"}'

# 2. Usar o token nos endpoints protegidos
TOKEN="seu-token-aqui"

# 3. Criar produto
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Anel Prata","category":"Aneis","pricingType":"FIXED","variants":[{"size":"18","sku":"ANEL-001","price":150}]}'

# 4. Adicionar estoque
curl -X POST http://localhost:8080/api/products/1/stock/movements \
  -H "Content-Type: application/json" \
  -d '{"variantId":1,"type":"IN","quantity":10,"reason":"entrada"}'

# 5. Registrar e logar como cliente
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Cliente","email":"cli@email.com","password":"123456"}'

CLI_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cli@email.com","password":"123456"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# 6. Adicionar ao carrinho
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CLI_TOKEN" \
  -d '{"variantId":1,"quantity":2}'

# 7. Finalizar pedido (WhatsApp)
curl -X POST http://localhost:8080/api/orders/1/checkout \
  -H "Authorization: Bearer $CLI_TOKEN"

# 8. Admin confirma pagamento
curl -X PATCH http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"status":"PAID"}'
```

## Endpoints por fase

| Fase | Descrição | Endpoints |
|---|---|---|
| 2 | Produtos e Estoque | `/api/products`, `/api/products/*/stock` |
| 3 | Região de Entrega | `/api/addresses/validate` |
| 4 | Carrinho | `/api/cart` |
| 5 | Checkout WhatsApp | `/api/orders/*/checkout` |
| 6 | Status do Pedido | `/api/orders`, `/api/orders/*/status` |
| 7 | Autenticação | `/api/auth` |

## Estrutura do projeto

```
src/main/java/com/trie/ecommerce/
├── config/        # Configurações (CORS, ShedLock, Security)
├── controller/    # Endpoints REST
├── dto/           # Data Transfer Objects
│   ├── request/
│   └── response/
├── entity/        # Entidades JPA
├── enums/         # Enumeradores
├── exception/     # Exceções + Handler global
├── mapper/        # Conversores Entity ↔ DTO
├── repository/    # Interfaces JPA
├── security/      # JWT + Filtros + UserDetails
└── service/       # Lógica de negócio
```
