import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';

/** Refetch das métricas Redis; um pouco menor que o tick do backend (~10s) para acompanhar mudanças. */
const POLL_METRICAS_MS = 5_000;

function coverSrc(game) {
  if (!game.capaDisponivel || !game.id) return '/gamehub.png';
  return `/mongo/arquivos/${game.id}/imagem`;
}

function Jogos() {
  const navigate = useNavigate();
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState(null);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('Todos');
  const [modeFilter, setModeFilter] = useState('Todos');

  useEffect(() => {
    let cancelado = false;
    let intervalId = null;

    async function carregar(primeiraVez) {
      if (primeiraVez) {
        setLoading(true);
        setErro(null);
      }
      try {
        const res = await fetch('/stats/catalogo/metricas');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        if (!cancelado) setGames(Array.isArray(data) ? data : []);
        if (!cancelado && primeiraVez) setErro(null);
      } catch {
        if (!cancelado && primeiraVez) {
          setErro('Não foi possível carregar o catálogo com métricas Redis. Verifique backend e Redis.');
          setGames([]);
        }
      } finally {
        if (!cancelado && primeiraVez) setLoading(false);
      }
    }

    carregar(true);
    intervalId = window.setInterval(() => { carregar(false); }, POLL_METRICAS_MS);
    return () => {
      cancelado = true;
      window.clearInterval(intervalId);
    };
  }, []);

  const statusOpcoes = useMemo(() => {
    const conjunto = [...new Set(games.map(g => g.status).filter(Boolean))];
    return ['Todos', ...conjunto.sort((a, b) => String(a).localeCompare(String(b), 'pt-BR'))];
  }, [games]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return games.filter((g) => {
      const nome = (g.nome || '').toLowerCase();
      const desc = (g.descricao || '').toLowerCase();
      const plats = g.plataformasPublicacao || [];
      const matchSearch = !q
        || nome.includes(q)
        || desc.includes(q)
        || plats.some((p) => p.toLowerCase().includes(q));
      const matchStatus = statusFilter === 'Todos' || g.status === statusFilter;
      const matchMode = modeFilter === 'Todos' || g.modoJogo === modeFilter;
      return matchSearch && matchStatus && matchMode;
    });
  }, [games, search, statusFilter, modeFilter]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>

      <nav style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '15px 30px',
        backgroundColor: '#333',
        color: 'white'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          <div style={{ fontSize: '1.2rem', cursor: 'pointer' }} onClick={() => navigate('/')}>GameHUB</div>
          <a href="/jogos" style={{ color: '#fff', textDecoration: 'none', fontSize: '0.85rem', fontWeight: '600' }}>Jogos</a>
          <a href="/rankings" style={{ color: '#ccc', textDecoration: 'none', fontSize: '0.8rem' }}>Rankings</a>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <button
            onClick={() => navigate('/login')}
            style={{ background: 'transparent', color: 'white', border: '1px solid white', padding: '8px 15px', borderRadius: '5px', cursor: 'pointer' }}
          >
            Entrar
          </button>
          <button
            onClick={() => navigate('/register')}
            className="btn-primary"
            style={{ padding: '8px 15px', border: 'none', cursor: 'pointer' }}
          >
            Cadastrar Empresa
          </button>
        </div>
      </nav>

      <header style={{ textAlign: 'center', padding: '40px 20px 12px' }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Catálogo de Jogos</h1>
        <p style={{ color: '#999', fontSize: '0.95rem', maxWidth: '640px', margin: '0 auto' }}>
          Dados do MongoDB com métricas do Redis (online, pico, popularidade). Atualização automática a cada {POLL_METRICAS_MS / 1000}s.
        </p>
      </header>

      <div style={{ maxWidth: '1100px', margin: '0 auto', width: '100%', padding: '0 20px' }}>

        {loading && (
          <p style={{ color: '#888', textAlign: 'center', padding: '32px 0' }}>Carregando jogos…</p>
        )}
        {erro && !loading && (
          <p style={{ color: '#e88', textAlign: 'center', padding: '16px' }}>{erro}</p>
        )}

        {!loading && !erro && games.length === 0 && (
          <div style={{ textAlign: 'center', padding: '48px 20px', color: '#888' }}>
            <p style={{ marginBottom: '8px' }}>Nenhum jogo cadastrado no Mongo.</p>
          </div>
        )}

        {!loading && !erro && games.length > 0 && (
        <>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px', alignItems: 'center', marginBottom: '24px' }}>
          <input
            type="text"
            className="form-control"
            placeholder="Buscar por nome, descrição ou plataforma…"
            value={search}
            onChange={e => setSearch(e.target.value)}
            style={{ maxWidth: '320px', flex: '1' }}
          />
          <select
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value)}
            className="form-control"
            style={{ width: 'auto', maxWidth: '180px' }}
          >
            {statusOpcoes.map(s => (
              <option key={s} value={s}>{s === 'Todos' ? 'Todos os status' : s}</option>
            ))}
          </select>
          <select
            value={modeFilter}
            onChange={e => setModeFilter(e.target.value)}
            className="form-control"
            style={{ width: 'auto', maxWidth: '180px' }}
          >
            <option value="Todos">Todos os modos</option>
            <option value="Singleplayer">Singleplayer</option>
            <option value="Multiplayer">Multiplayer</option>
            <option value="Ambos">Ambos</option>
          </select>
          <span style={{ color: '#888', fontSize: '0.85rem', marginLeft: 'auto' }}>
            {filtered.length} jogo{filtered.length !== 1 ? 's' : ''} exibido{filtered.length !== 1 ? 's' : ''}
          </span>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '20px', paddingBottom: '40px' }}>
          {filtered.map((game) => {
            const plats = game.plataformasPublicacao || [];
            const loja = plats[0] || '—';
            const online = game.jogadoresOnline ?? 0;
            const pico = game.picoJogadoresOnline ?? 0;
            const pop = game.scorePopularidade ?? 0;
            return (
              <div key={game.id} className="card" style={{ padding: '0', overflow: 'hidden', transition: 'transform 0.2s', cursor: 'pointer' }}>
                <img
                  src={coverSrc(game)}
                  alt={game.nome || ''}
                  style={{ width: '100%', height: '140px', objectFit: 'cover', display: 'block', background: '#2a2a2a' }}
                  onError={(e) => { e.currentTarget.src = '/gamehub.png'; }}
                />
                <div style={{ padding: '14px' }}>
                  <h3 style={{ fontSize: '1rem', margin: '0 0 4px', borderBottom: 'none' }}>{game.nome || '—'}</h3>
                  <p style={{ color: '#999', fontSize: '0.8rem', margin: '0 0 8px' }}>{loja}</p>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '6px', marginBottom: '10px', fontSize: '0.68rem', textAlign: 'center' }}>
                    <span style={{ padding: '4px', background: '#1e3d1e', borderRadius: '6px', color: '#9f9' }}>
                      Online<br /><strong>{online}</strong>
                    </span>
                    <span style={{ padding: '4px', background: '#333', borderRadius: '6px', color: '#ccc' }}>
                      Pico<br /><strong>{pico}</strong>
                    </span>
                    <span style={{ padding: '4px', background: '#1a2744', borderRadius: '6px', color: '#9ec8ff' }}>
                      Pop.<br /><strong>{typeof pop === 'number' ? pop.toLocaleString('pt-BR', { maximumFractionDigits: 0 }) : pop}</strong>
                    </span>
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginBottom: '10px' }}>
                    <span style={{ fontSize: '0.7rem', padding: '2px 8px', background: '#333', borderRadius: '10px', color: '#ccc' }}>{game.status || '—'}</span>
                    <span style={{ fontSize: '0.7rem', padding: '2px 8px', background: '#333', borderRadius: '10px', color: '#ccc' }}>{game.modoJogo || '—'}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: '0.85rem', color: '#cce', fontWeight: '600' }}>{game.preco || '—'}</span>
                    <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
                      {plats.slice(0, 2).map((p) => (
                        <span key={p} style={{ fontSize: '0.6rem', padding: '2px 6px', border: '1px solid #444', borderRadius: '4px', color: '#aaa' }}>{p}</span>
                      ))}
                      {plats.length > 2 && (
                        <span style={{ fontSize: '0.6rem', padding: '2px 6px', border: '1px solid #444', borderRadius: '4px', color: '#aaa' }}>+{plats.length - 2}</span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        {filtered.length === 0 && games.length > 0 && (
          <div style={{ textAlign: 'center', padding: '60px 20px', color: '#888' }}>
            <p style={{ fontSize: '2rem', marginBottom: '8px' }}>😕</p>
            <p>Nenhum jogo encontrado com esses filtros.</p>
          </div>
        )}
        </>
        )}
      </div>

      <footer style={{
        textAlign: 'center',
        padding: '20px',
        backgroundColor: '#333',
        color: 'white',
        marginTop: 'auto'
      }}>
        <p style={{ margin: 0 }}>&copy; {new Date().getFullYear()} GameHUB. Todos os direitos reservados.</p>
        <p style={{ fontSize: '0.8rem', color: '#aaa', marginTop: '5px' }}>Projeto Banco de Dados</p>
      </footer>
    </div>
  );
}

export default Jogos;
