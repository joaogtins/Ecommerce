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
