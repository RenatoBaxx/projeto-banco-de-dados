# Redis — endpoints e funcionamento

Base URL no backend: **`/api/stats`**. O Redis guarda estado “quente”: contadores por jogo, utilizadores online (sets), ranking por popularidade (ZSET) e **cache do catálogo** (`catalog:*`) espelhado a partir do Mongo. Não substitui o Mongo como fonte de verdade dos jogos.

## Endpoints

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `GET` | `/api/stats/ranking` | Até **10** ids de jogos mais populares: `ZSET` `ranking:games` por ordem decrescente de score (`reverseRange` 0–9). |
| `GET` | `/api/stats/ranking/atividade` | Ranking enriquecido por atividade; query opcional `limite` (número). |
| `GET` | `/api/stats/catalogo/metricas` | Catálogo com métricas (agrega Redis + dados vindos do Mongo). |
| `GET` | `/api/stats/catalogo/ranking-cache` | Ordem do ranking só a partir do cache `catalog:*` (warmup / sync desde o Mongo). |
| `POST` | `/api/stats/{gameId}/enter` | Entrada de jogador: query `userId`; atualiza hashes `game:{id}:stats`, set `game:{id}:users`, score em `ranking:games`. |
| `POST` | `/api/stats/{gameId}/leave` | Saída de jogador: query `userId`; decrementa online. |
| `GET` | `/api/stats/{gameId}` | Estatísticas agregadas (`online`, `max`, `min`) para o jogo, ou 404. |
| `GET` | `/api/stats/{gameId}/users` | Conjunto de ids de utilizadores no set online do jogo. |

**Nota:** Rotas literais (`/ranking`, `/catalogo/...`) estão registadas antes de `/{gameId}` para não colidirem com ids.

## Modelo de dados no Redis (chaves principais)

- `game:{gameId}:stats` — hash (`online`, `max`, `min`).
- `game:{gameId}:users` — set de `userId` em sessão.
- `ranking:games` — ZSET (score incrementado em cada `enter`).
- `catalog:ranking:order` — lista com ordem dos ids do catálogo em cache.
- `catalog:ranking:ids` — set dos ids presentes no cache.
- `catalog:game:{id}` — hash com campos públicos do jogo para listagens rápidas.

## Como funciona (resumo)

1. **`GameStatsService`**: leitura/escrita das chaves por jogo, ranking por atividade e catálogo com métricas (consulta também `JogoRepository` para nomes/detalhes).
2. **`CatalogoJogoRankingService`**: no arranque ou após mudanças no Mongo, repovoa ou atualiza `catalog:*`; `listarRanking` serve a rota `ranking-cache`.
3. **Scheduler opcional** (`application.properties`): simulação de jogadores online altera contagens periodicamente em desenvolvimento.

Implementação: `GameStatsController`, `GameStatsService`, `CatalogoJogoRankingService`.
