# Triê Pratas — Frontend

Vitrine de e-commerce em React + TypeScript + Vite + Tailwind CSS v4, consumindo a API
Spring Boot deste mesmo repositório.

## Como rodar

```bash
cd front
npm install
npm run dev
```

Em dev, o Vite faz proxy de `/api/*` para `http://localhost:8080` (ver `vite.config.ts`),
então basta o backend estar rodando (`./mvnw spring-boot:run` na raiz do repo).

## Telas implementadas

| Tela | Rota | Endpoints usados |
|---|---|---|
| Home / Destaques | `/` | `GET /api/products/featured`, `/api/products/new-collection` |
| Catálogo | `/produtos` | `GET /api/products`, `/api/products/search`, `/api/products/categories` |
| Detalhe do produto | `/produtos/:id` | `GET /api/products/{id}`, `POST /api/cart/items` |
| Login / Cadastro | `/entrar` | `POST /api/auth/login`, `/api/auth/register` |
| Carrinho | `/carrinho` (autenticado) | `GET /api/cart`, `DELETE /api/cart/items/{id}`, `POST /api/orders/{id}/checkout` |
| Meus pedidos | `/pedidos` (autenticado) | `GET /api/orders/me` |

## Ainda não implementado (aguardando definição de layout)

- **Validação de endereço / entrega** — endpoint `POST /api/addresses/validate` já existe no
  backend, mas não há uma tela dedicada no Figma; hoje o endereço aparece fixo no checkout.
- **Telas de admin** (gestão de produtos, estoque, pedidos) — endpoints existem, telas ainda
  não foram desenhadas.

## Estrutura

```
src/
├── api/          # chamadas HTTP por domínio (products, cart, orders, auth)
├── components/   # Header, Footer, Layout, ProductCard, ProtectedRoute
├── context/      # AuthContext (JWT + usuário) e CartContext (carrinho)
├── pages/        # uma página por rota
├── types/        # tipos TS espelhando os DTOs do backend
└── utils/        # formatação de preço, data, status
```

## Autenticação

O token JWT retornado por `/api/auth/login` e `/api/auth/register` é salvo no
`localStorage` (`trie_token`) e enviado como `Authorization: Bearer` em toda chamada
autenticada (`src/api/client.ts`). Rotas de carrinho e pedidos são protegidas por
`ProtectedRoute`, que redireciona para `/entrar` se não houver sessão.
