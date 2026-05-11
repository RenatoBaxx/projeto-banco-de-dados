# Perguntas do grupo — respostas (Polyglot Persistence)

Documento para defesa / relatório. Os endpoints detalhados por banco estão em `Backend/Mongo/ENDPOINTS.md`, `Backend/Redis/ENDPOINTS.md` e `Backend/Supabase/ENDPOINTS.md`.

---

## 1. Qual o tema do projeto?

**Hub de jogos / catálogo digital**: registo e autenticação de utilizadores (empresas), **CRUD de jogos** com documentos ricos no MongoDB, ficheiros no servidor, e **métricas em tempo real** (online, ranking, cache de catálogo) no Redis, com dados relacionais de **empresas** no PostgreSQL exposto pelo Supabase.

---

## 2. Quem são os integrantes do grupo?

**Não consta no repositório** (nenhum ficheiro lista nomes). Preencher com os nomes e identificadores exigidos pela disciplina.

---

## 3. Quais bancos serão usados? Motivo da escolha

| Banco / serviço | Papel | Motivo |
|-----------------|--------|--------|
| **PostgreSQL (via Supabase)** | Relacional: utilizadores (Auth) e tabela `empresas` | Modelo tabular, integridade referencial, transações e políticas de acesso (RLS no Supabase) adequados a cadastro e login. |
| **MongoDB** | NoSQL documental: entidade **Jogo** | Esquema flexível para metadados variados (plataformas, preço, estado, referência a ficheiros) e evolução do documento sem migrações pesadas. |
| **Redis** | NoSQL em memória: stats, sets, ZSET, cache `catalog:*` | Baixa latência para contagens, ranking e vistas agregadas; dados derivados ou “quentes” sem sobrecarregar o Mongo em cada leitura. |

---

## 4. Qual linguagem será usada em cada parte? Motivo da escolha

| Parte | Linguagem | Motivo típico (defesa) |
|-------|-----------|-------------------------|
| **Backend** | **Java** (Spring Boot, Gradle) | Ecossistema maduro, tipagem forte, integração estável com drivers Mongo/Redis e cliente HTTP; adequado a API REST e regras de negócio. |
| **Frontend** | **JavaScript (React)** + **Vite** | Componentização, ecossistema de UI, proxy de desenvolvimento para a API; curva comum em disciplinas web. |
| **Config / contratos** | YAML, `.properties`, JSON nas APIs | Padrão do ecossistema Spring e REST. |

---

## 5. Considerando o teorema CAP, o que pode acontecer quando cada um dos bancos não estiver disponível?

O CAP trata de **partições de rede** (P) e do equilíbrio entre **consistência** (C) e **disponibilidade** (A). Na prática, “indisponível” costuma ser falha de serviço ou partição.

### PostgreSQL / Supabase indisponível

- Registo, login e `/me` falham.
- O backend tende a **recusar** ou devolver erro nessas operações: perde-se **disponibilidade** dessa parte em troca de não afirmar estados falsos sobre dados no servidor (alinhado a **consistência forte** no primário quando o serviço responde).

### MongoDB indisponível

- **CRUD de jogos** e fluxos que dependem do Mongo deixam de ter fonte de verdade atualizada.
- Pode haver **leituras desatualizadas** no Redis até nova sincronização. Risco de **inconsistência** entre cache (`catalog:*`) e o estado real que existiria no Mongo.

### Redis indisponível

- Rotas sob `/api/stats` falham ou devolvem erro.
- **Jogos** no Mongo podem continuar a funcionar se só se use o `JogoController`.
- Popularidade, online e ranking deixam de atualizar ou de ser lidos: perde-se **disponibilidade** dessa funcionalidade; o catálogo “oficial” no Mongo pode manter-se coerente, sem a camada rápida.

---

## 6. Descreva como cada um dos bancos trabalha com o princípio de consistência

### PostgreSQL (Supabase)

- **Consistência forte** no primário: transações ACID, leitura do que foi commitado (com nível de isolamento configurável).
- Réplicas podem ser **atrasadas** (replicação assíncrona); leituras direcionadas a réplicas podem ser **eventualmente consistentes** — depende da configuração do provedor.

### MongoDB

- Depende de **`writeConcern`** e **`readConcern`**. Com `majority` e leituras alinhadas (por exemplo no primário), aproxima-se de **linearizabilidade** por documento no replica set.
- Leituras em secundários sem cuidado adequado podem ver dados **atrasados**.

### Redis

- **Instância única**: comandos são essencialmente **sequenciais** e fortemente consistentes nesse nó.
- **Redis Cluster / réplicas**: escrita no primário da shard; réplicas assíncronas → leituras na réplica podem ser **eventualmente consistentes**. O modelo exato segue o deployment (standalone vs cluster).

---

## 7. NoSQL com réplicas e/ou particionamento — falha de instância e consistência

**Nota:** em **MongoDB Atlas** e **Redis Cloud** a topologia (número de nós, quorum) depende do plano e da região. Abaixo segue o modelo **clássico** para defesa.

### MongoDB (replica set)

- Escrita com **`w: majority`**: tolera falha de **minoria** de votantes (ex.: 3 nós → 1 indisponível; 5 nós → até 2, se a maioria eleitoral se mantiver).
- Se a **maioria** dos membros cair, o set pode **não eleger primário** → **sem escrita** (prioridade a **C** sobre **A** nesse cenário).
- Em **partição de rede**, o protocolo de eleição evita dois primários ativos com maioria; só o lado com **maioria** continua a aceitar escritas coerentes.

### Redis

- **Standalone**: uma instância fora = perda dessa camada até recuperação (sem failover automático).
- **Sentinel + réplica**: falha do primário → promoção se os sentinels tiverem **quorum**; breve perda de **A** nas escritas até o failover. Réplicas podem ir **ligeiramente atrás** da primária (replicação assíncrona).
- **Cluster**: cada slot tem primário; o cluster precisa de **maioria de masters acessíveis** para operação válida. Consistência é **por chave** / shard, não global como num único nó SQL.

### Quantas instâncias podem falhar “e ainda garantir consistência”?

- Não há número universal: depende de **N**, **quorum** e **write concern / waits**.
- **MongoDB** com replica set e `majority`: mantém-se consistência de escrita enquanto existir **maioria de membros vivos e comunicantes**; num set típico de **N** membros com votos, até **⌊(N−1)/2⌋** nós podem falhar (regra ilustrativa; validar com a documentação oficial e a vossa topologia).
- **Redis** em cluster: o limite vem do **quorum do cluster e dos primários por slot**; para produção gerida, indicar que **depende da configuração do Redis Cloud** é correto.

---

## Referência cruzada

- Endpoints Mongo: `src/main/java/com/projetoDados/HUB/Backend/Mongo/ENDPOINTS.md`
- Endpoints Redis: `src/main/java/com/projetoDados/HUB/Backend/Redis/ENDPOINTS.md`
- Endpoints Supabase / Auth: `src/main/java/com/projetoDados/HUB/Backend/Supabase/ENDPOINTS.md`
