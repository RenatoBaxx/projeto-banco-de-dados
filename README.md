# projeto-banco-de-dados

Monorepo com backend **Spring Boot** (Java 25) e frontend **React + Vite**. O backend expõe uma API sob o prefixo **`/api`** e integra **MongoDB** (dados dos jogos), **Redis** (métricas e cache em memória) e **Supabase** (autenticação e registo de empresas).

## Pré-requisitos

- **JDK 25** (toolchain definida no Gradle).
- **Node.js** em versão **LTS** atual (ex.: 20.x ou 22.x) para o frontend.
- Contas em serviços **cloud**: cluster MongoDB (ex.: Atlas), instância Redis (ex.: Redis Cloud), projeto Supabase com Auth ativo e tabela `empresas` conforme o backend espera.

## Variáveis de ambiente

Crie um ficheiro `.env` na raiz do projeto (ou exporte no sistema).

| Variável | Descrição |
|----------|-----------|
| `MONGO_URI_AKIRA` | URI de ligação ao MongoDB. |
| `REDIS_HOST` | Host do Redis. |
| `REDIS_PORT` | Porta (número). |
| `REDIS_PASSWORD` | Palavra-passe, se aplicável. |
| `SUPABASE_URL` | URL base do projeto. |
| `SUPABASE_KEY` | Chave `service_role` ou `anon` conforme a política do teu projeto. |
| `VITE_PROXY_TARGET` | **Só para o frontend (Vite):** URL base do Spring Boot que recebe o proxy de `/api` e `/uploads`. Omissão: `http://127.0.0.1:8080`. Exemplo PowerShell antes de `npm run dev`: `$env:VITE_PROXY_TARGET='http://127.0.0.1:9090'`. |

O `springboot4-dotenv` (dependência de desenvolvimento) pode carregar `.env` ao correr o backend; confirma na documentação da dependência se o ficheiro está no path esperado. Para o Vite, variáveis `VITE_*` podem ir num `.env` dentro de `frontend/` (ver [documentação Vite](https://vite.dev/guide/env-and-mode.html)).

## Como rodar o backend

Na raiz do repositório (onde está `build.gradle`):

```bash
./gradlew bootRun
```

No Windows (PowerShell):

```powershell
.\gradlew.bat bootRun
```

Por omissão o servidor escuta na porta **8080**. Perfil opcional **`dev`** ativa o endpoint de teste Redis em `/redis-test` (`RedisTestController`).

## Como rodar o frontend

```bash
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`. O Vite faz **proxy** de `/api` e `/uploads` para o valor de **`VITE_PROXY_TARGET`** (por omissão `http://127.0.0.1:8080`). Se o backend estiver outra porta ou máquina, define essa variável antes de `npm run dev`.

## API principal (prefixo `/api`)

- **`/api/jogos/**`** — CRUD e publicação multipart dos jogos (Mongo + ficheiros em disco + sync Redis `catalog:*`).
- **`/api/stats/**`** — Ranking por ZSET, catálogo com métricas, enter/leave, stats por jogo; **`GET /api/stats/catalogo/ranking-cache`** substitui o antigo `GET /catalog/ranking`.
- **`/api/auth/**`** — `POST /register`, `POST /login`, `GET /me` (Bearer token). Respostas de sucesso são JSON tipado (`accessToken`, `refreshToken`, etc.).

## Por que cada banco / serviço

**MongoDB** — Guarda o documento principal de cada jogo (`ArquivoDocumento`: nome, preço, plataformas, estado, bytes da capa, etc.) e é a fonte de verdade para o catálogo. Os ficheiros `.zip` grandes ficam no disco do servidor; o Mongo guarda o caminho.

**Redis** — Memória de baixa latência para:
- hashes `game:{id}:stats` (online, pico, mínimo) e sets de utilizadores em sessão;
- ZSET `ranking:games` para ordenar por popularidade quando há eventos `enter`;
- cache `catalog:*` (lista ordenada + hashes) espelhado a partir do Mongo no arranque e após alterações.

Não substitui o Mongo para dados permanentes do jogo; complementa com estado “quente” e vistas rápidas.

**Supabase** — Autenticação (signup/login com JWT) e inserção de linhas na tabela `empresas` via REST, alinhado ao registo de empresas no frontend.

## Desenvolvimento na IDE

Podes usar a extensão **Gradle** no VS Code / Cursor e executar a tarefa **bootRun** em **Tasks → application**, em alternativa à linha de comandos.
