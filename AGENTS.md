# Regras de Trabalho — Triê E-commerce

## Commits

- **Pergunte antes de commitar.** Antes de qualquer `git commit`, pergunte se o usuário quer prosseguir. Mostre um resumo do que será commitado (arquivos modificados + mudanças relevantes).
- Commits com mensagens descritivas em português no formato: `<tipo>: <resumo>`. Tipos: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`.
- Nunca force push (`git push --force`). Nunca altere commits já enviados.

## Validação

- Após qualquer alteração em arquivos Java ou no `pom.xml`, execute `./mvnw compile -q` para verificar que compila.
- Se alterar configurações (application.yml, docker-compose), tente validar com `./mvnw spring-boot:run` por alguns segundos se possível.
- Se houver testes, execute `./mvnw test` nas fases relevantes.

## Changelog

- Atualize `CHANGELOG.md` **a cada commit**, não apenas no fim da fase.
- Inclua: arquivos criados/modificados, desvios do plano original, e motivos técnicos por trás de cada decisão.

## Segurança

- **Nunca exiba tokens, senhas ou chaves de API nos outputs.** Substitua por `***` se for necessário referenciar.
- Nunca commite tokens reais em arquivos de configuração.

## Condução do trabalho

- Siga a ordem do roadmap: Fase 0 → 1 → 2 → ... → 9.
- Antes de iniciar uma nova fase, verifique se a fase anterior está completa e funcional.
- Se encontrar um problema técnico (versão incompatível, dependência faltando), **explique o problema e a solução** — não apenas aplique a correção.
- Ao final de cada passo, explique brevemente **o que foi feito** e **por que** (contexto técnico).

## Destrutivos

- Pergunte antes de: deletar arquivos, rodar `DROP TABLE`, alterar migrações Flyway já aplicadas, ou modificar o arquivo `opencode.json`.
- Não remova imports ou dependências que podem estar sendo usadas em outras fases futuras.
