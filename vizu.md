## Objetivo
Refatorar **somente o backend** deste projeto (Spring Boot), deixando o código **o mais simples e didático possível para iniciantes**, **sem alterar a lógica de negócio nem o comportamento observável** (mesmas rotas, mesmos contratos de API, mesma integração com MongoDB, Redis e Supabase).

## Regras
- Não mudar regras de negócio: apenas reorganizar, renomear com critério, extrair métodos, reduzir complexidade superficial e **documentar com comentários** o que cada trecho faz e **o que aquela parte entrega** (entrada/saída ou efeito colateral quando fizer sentido).
- Manter compatibilidade: endpoints, DTOs expostos, formatos JSON relevantes para o front.
- Escopo: **backend Java** apenas (não refatorar frontend neste plano).
- Ao final do trabalho: **atualizar o README** com (1) como rodar o projeto (backend + variáveis de ambiente + Redis/Mongo/Supabase) e (2) **por que** cada banco/serviço foi escolhido (papel de cada um na arquitetura).

## Informações que já tenho
- Quero classes mais curtas e legíveis, comentários explicando propósito e “o que entrega”, estilo iniciante.
- README deve refletir o estado pós-refatoração e as decisões de persistência.

## Preencha (suas respostas alimentam o plano)
1. **Prioridade de pastas/pacotes:** Quais áreas do backend mais te incomodam hoje? (ex.: `Mongo`, `Redis`, `Supabase/Auth`, controllers, serviços gigantes.)
2. **Critério de “simples”:** Prefere mais classes pequenas (muitos arquivos) ou menos arquivos com métodos bem nomeados e comentários? Há limite de tamanho por classe (ex.: máx. ~200 linhas)?
3. **Idioma:** Comentários e README em **português** (confirmar) ou bilíngue?
4. **Testes:** Devemos **adicionar/ajustar testes** durante a refatoração para garantir que a lógica não mudou, ou só refatorar e rodar testes existentes?
5. **Breaking changes:** O front em produção/homologação **não pode** mudar nada na API — confirma? Algum endpoint “legacy” que possa ser mantido só com `@Deprecated`?
6. **Ambiente:** README deve focar em **Windows + PowerShell**, **Linux**, ou ambos? Usa **Docker** para Mongo/Redis ou só serviços cloud (Atlas, Redis Cloud)?
7. **Segredos:** No README, prefere exemplos com **placeholders** (`MONGO_URI=...`) e nunca valores reais, certo?
8. **Ordem de execução:** Prefere refatorar em **fases** (ex.: 1) camada Redis, 2) Mongo, 3) Auth) ou **por feature vertical** (ex.: “jogos/arquivos” ponta a ponta)?
9. **Documentação extra:** Além do README, quer **diagrama simples** (Mermaid) da arquitetura no README ou só texto?
10. **Pós-refatoração:** Há alguma **métrica** desejada (ex.: “nenhum service > 300 linhas”, “cada controller só delega a um service”)?

## Entregáveis esperados do plano
- Lista ordenada de etapas com risco baixo (refactor mecânico antes de renomeações amplas).
- Mapa de pacotes sugerido (opcional) alinhado a iniciantes.
- Checklist de verificação: build, testes, smoke manual dos endpoints críticos.
- Esboço das seções do README (como rodar + por que Mongo vs Redis vs Supabase, etc.).

Gere o plano só depois de incorporar minhas respostas acima. Se alguma resposta estiver em branco, assuma padrões conservadores e declare as suposições no plano.