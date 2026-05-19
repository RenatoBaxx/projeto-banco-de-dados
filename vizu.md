# --- Visão geral ---
DBSIZE
KEYS *

# --- CATÁLOGO ---
TYPE catalog:ranking:order
LLEN catalog:ranking:order
LRANGE catalog:ranking:order 0 -1

SCARD catalog:ranking:ids
SMEMBERS catalog:ranking:ids

# Pegue um ID da lista acima e substitua {ID} abaixo:
# KEYS catalog:game:*
# HGETALL catalog:game:{ID}

# Mostra a tabela de usuarios dos jogos
ZREVRANGE ranking:games 0 9 WITHSCORES

# Para cada jogo (substitua {ID}):
# HGETALL game:{ID}:stats
# SMEMBERS game:{ID}:users
# TYPE game:{ID}:stats
# TYPE game:{ID}:users

# --- Chaves só de métricas / usuários ---
KEYS game:*:stats
KEYS game:*:users