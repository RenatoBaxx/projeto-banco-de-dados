# projeto-banco-de-dados


Como rodar:

Baixar o java jdk 25.

no vscode, baixar a extensão do gradle.
abrir ele, localizado na barra esquerda.
e ir no "Task" - "application" - "bootRun"
 
Isso faz o servidor rodar.

Baixar o nodeJS v11.9.0

no vscode abra após baixar, abra o diretorio "./frontend"
abra o terminal e execute "npm install"
logo após a instalaçao, para rodar execute no terminal novamente "npm run dev"
e acesse o site em local de desenvolvimento no url "http://localhost:5173/"

Isso faz o nosso Front-End executar.
Assim já temos tanto nosso FE + BE rodando e o projeto funcional.


Motivos das Escolhas:

O Redis foi escolhido para gerenciar as operações assíncronas de publicação de jogos. Como o Redis é um banco de dados em memória extremamente rápido e suporta estruturas de dados como listas e hashes, ele é ideal para implementar filas de processamento e armazenar estados temporários de operações. Nesse projeto, o Redis é utilizado para controlar a fila de uploads e o status de publicação dos jogos nas plataformas externas.

Redis não é usado para CRUD tradicional de dados permanentes. Ele é usado para cache, filas e estados temporários, então usar ele para fila de upload + status é arquitetura correta.

Supabase (PostgreSQL) foi escolhido para gerenciar a autenticação de usuários e o armazenamento permanente dos dados das empresas. Como o Supabase oferece um banco PostgreSQL completo com API REST automática e um sistema de autenticação integrado (JWT), ele é ideal para operações que exigem persistência, integridade relacional e segurança. Nesse projeto, o Supabase é utilizado para cadastrar e autenticar empresas (login/registro), armazenar os dados cadastrais (nome, CNPJ, email) e fornecer o user_id único que vincula cada empresa aos seus jogos e uploads nos outros bancos.

Supabase não é usado para cache ou filas temporárias. Ele é usado para dados permanentes e autenticação, então usar ele para registro de empresas + controle de identidade é arquitetura correta.

