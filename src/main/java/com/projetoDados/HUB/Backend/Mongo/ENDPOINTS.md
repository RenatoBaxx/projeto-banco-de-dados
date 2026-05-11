# MongoDB — endpoints e funcionamento

Base URL no backend: **`/api/jogos`**. O módulo persiste documentos **`Jogo`** (Spring Data MongoDB). Ficheiros `.zip` grandes ficam em disco (`app.uploads.jogos-dir`); o Mongo guarda metadados e referência ao ficheiro. Após criar, atualizar ou apagar um jogo, o serviço sincroniza o espelho do catálogo no **Redis** (`catalog:*`) via `CatalogoJogoRankingService`.

## Endpoints

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `POST` | `/api/jogos` | Cria jogo só com JSON (`Content-Type: application/json`). Corpo: entidade `Jogo`. |
| `POST` | `/api/jogos/publicar` | Cria jogo com **multipart**: `nome`, `descricao`, `preco`, `os` (JSON string), `modo`, `platforms` (JSON string), `imagem` (ficheiro), `arquivo` (.zip). |
| `GET` | `/api/jogos/dashboard` | Lista resumo para painel (id, loja, status). |
| `GET` | `/api/jogos` | Lista todos os documentos completos. |
| `GET` | `/api/jogos/{id}/imagem` | Devolve bytes da capa e `Content-Type`, ou 404. |
| `GET` | `/api/jogos/{id}` | Busca um jogo por id, ou 404. |
| `PUT` | `/api/jogos/{id}` | Atualização completa por JSON; ou 404. |
| `PUT` | `/api/jogos/{id}/status` | Atualiza só `status` (query `status=...`); ou 404. |
| `DELETE` | `/api/jogos/{id}` | Remove documento no Mongo, ficheiro `.zip` no disco e entrada no catálogo Redis; ou 404. |

## Como funciona (resumo)

1. **`JogoRepository`** (`MongoRepository<Jogo, String>`) executa as operações CRUD no cluster configurado em `spring.mongodb.uri`.
2. **Criação com upload**: validação de tipo/tamanho da imagem, parsing das listas JSON, gravação do documento, escrita do `{id}.zip` no diretório de uploads, atualização do caminho no documento.
3. **Consistência com Redis**: qualquer alteração relevante chama `sincronizarDocumentoNoRedis` / fluxos de delete para manter hashes e listas `catalog:*` alinhados ao Mongo (fonte de verdade do catálogo).

Implementação: `JogoController`, `JogoService`, `Jogo`, `JogoRepository`.
