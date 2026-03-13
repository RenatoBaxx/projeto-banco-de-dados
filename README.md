# projeto-banco-de-dados


Como rodar:

Baixar o java jdk 25.

no vscode, baixar a extensão do gradle.
abrir ele, localizado na barra esquerda.
e ir no "Task" - "application" - "bootRun"
 
Isso faz o servidor rodar.



O Redis foi escolhido para gerenciar as operações assíncronas de publicação de jogos. Como o Redis é um banco de dados em memória extremamente rápido e suporta estruturas de dados como listas e hashes, ele é ideal para implementar filas de processamento e armazenar estados temporários de operações. Nesse projeto, o Redis é utilizado para controlar a fila de uploads e o status de publicação dos jogos nas plataformas externas.

Redis não é usado para CRUD tradicional de dados permanentes. Ele é usado para cache, filas e estados temporários, então usar ele para fila de upload + status é arquitetura correta.