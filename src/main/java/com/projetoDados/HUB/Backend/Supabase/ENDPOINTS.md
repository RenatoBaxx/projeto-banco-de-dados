# Supabase (PostgreSQL + Auth) — endpoints e funcionamento

No projeto, **Supabase** concentra o **armazenamento relacional** (tabela `empresas` via REST) e **autenticação** (JWT). O backend Java não usa JDBC direto para essa base: usa **`RestClient`** contra a API HTTPS do projeto Supabase.

## Endpoints expostos pelo backend Spring (`/api/auth`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `POST` | `/api/auth/register` | Corpo JSON: `email`, `password`, `nomeEmpresa`, `cnpj`. Chama signup no Supabase Auth e, em sucesso, insere linha em `empresas` (se dados de empresa forem enviados). |
| `POST` | `/api/auth/login` | Corpo: `email`, `password`. Troca por tokens (`access_token`, `refresh_token`, etc.) via fluxo password do Supabase. |
| `GET` | `/api/auth/me` | Cabeçalho `Authorization: Bearer <access_token>`. Devolve identificador e email do utilizador autenticado. |

## Chamadas HTTP feitas pelo backend ao Supabase

Estas rotas são **internas** (servidor → Supabase), não são prefixadas por `/api` na tua app:

| Uso | Método | URI (relativa à `SUPABASE_URL`) |
|-----|--------|----------------------------------|
| Registo | `POST` | `/auth/v1/signup` |
| Login | `POST` | `/auth/v1/token?grant_type=password` |
| Perfil | `GET` | `/auth/v1/user` (com Bearer do utilizador) |
| Empresas | `POST` | `/rest/v1/empresas` (com `apikey` + `Authorization: Bearer <service_role ou chave configurada>`) |

Variáveis de ambiente: `SUPABASE_URL`, `SUPABASE_KEY` (ver `application.properties` e README na raiz).

## Como funciona (resumo)

1. **Registo**: cria utilizador no Auth; obtém `user_id`; monta JSON com `user_id`, `nome_empresa`, `cnpj`, `email` e envia para `POST /rest/v1/empresas`. Falha na gravação da empresa é registada em log sem invalidar necessariamente a resposta de sucesso do signup (ver código de `AuthService`).
2. **Login / me**: delegação total ao Supabase Auth; o Spring apenas repassa erros e formata respostas tipadas para o frontend.

Implementação: `AuthController`, `AuthService`, DTOs em `Supabase/DTO/AuthContracts`.
