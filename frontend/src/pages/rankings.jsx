import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';

const POLL_RANKING_MS = 5_000;

function formatScore(n) {
  if (n == null || Number.isNaN(n)) return '0';
  if (n >= 1000) return n.toLocaleString('pt-BR', { maximumFractionDigits: 0 });
  return n.toLocaleString('pt-BR', { minimumFractionDigits: 0, maximumFractionDigits: 1 });
}

function Rankings() {
  const navigate = useNavigate();
  const [ranking, setRanking] = useState([]);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState(null);

  useEffect(() => {
    let cancelado = false;
    let intervalId = null;

    async function carregar(primeiraVez) {
      if (primeiraVez) {
        setLoading(true);
        setErro(null);
      }
      try {
        const res = await fetch('/stats/ranking/atividade?limite=60');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        if (!cancelado) setRanking(Array.isArray(data) ? data : []);
        if (!cancelado && primeiraVez) setErro(null);
      } catch {
        if (!cancelado && primeiraVez) {
          setErro('Não foi possível carregar o ranking. Confira backend e Redis.');
          setRanking([]);
        }
      } finally {
        if (!cancelado && primeiraVez) setLoading(false);
      }
    }

    carregar(true);
    intervalId = window.setInterval(() => { carregar(false); }, POLL_RANKING_MS);
    return () => {
      cancelado = true;
      window.clearInterval(intervalId);
    };
  }, []);

  const medalColor = (pos) => {
    if (pos === 1) return '#ffd700';
    if (pos === 2) return '#c0c0c0';
    if (pos === 3) return '#cd7f32';
    return '#666';
  };

  const topThree = ranking.slice(0, 3);

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
          <a href="/jogos" style={{ color: '#ccc', textDecoration: 'none', fontSize: '0.8rem' }}>Jogos</a>
          <a href="/rankings" style={{ color: '#fff', textDecoration: 'none', fontSize: '0.85rem', fontWeight: '600' }}>Rankings</a>
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
        <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Ranking de Jogos</h1>
        <p style={{ color: '#999', fontSize: '0.95rem', maxWidth: '640px', margin: '0 auto' }}>
          Ordem pelo Redis (ZSET; também sobe com <code>/stats/&lt;id&gt;/enter</code>). Online e pico vêm dos hashes por jogo.
          Lista atualiza automaticamente a cada {POLL_RANKING_MS / 1000}s.
        </p>
      </header>

      {loading && (
        <p style={{ textAlign: 'center', color: '#999', padding: '24px' }}>Carregando ranking…</p>
      )}
      {erro && !loading && (
        <p style={{ textAlign: 'center', color: '#e88', padding: '16px 20px' }}>{erro}</p>
      )}

      {!loading && !erro && ranking.length === 0 && (
        <p style={{ textAlign: 'center', color: '#999', padding: '24px', maxWidth: '520px', margin: '0 auto' }}>
          Nenhum jogo cadastrado. Publique pelo dashboard ou aguarde o primeiro jogador registrar entrada no jogo para subir no ZSET.
        </p>
      )}

      {!loading && ranking.length > 0 && (
      <div style={{ maxWidth: '1020px', margin: '0 auto', width: '100%', padding: '0 20px' }}>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '20px', marginBottom: '32px', flexWrap: 'wrap' }}>
          {topThree.map((game) => {
            const pos = game.posicao ?? 0;
            return (
              <div key={game.id} style={{
                flex: '0 1 260px',
                background: '#2a2a2a',
                borderRadius: '10px',
                overflow: 'hidden',
                border: `2px solid ${medalColor(pos)}`,
                textAlign: 'center',
              }}>
                <img
                  src={game.capaDisponivel ? `/mongo/arquivos/${game.id}/imagem` : '/gamehub.png'}
                  alt=""
                  style={{ width: '100%', height: '120px', objectFit: 'cover', background: '#1a1a1a' }}
                  onError={(e) => { e.currentTarget.src = '/gamehub.png'; }}
                />
                <div style={{ padding: '14px' }}>
                  <span style={{ fontSize: '1.5rem', fontWeight: '700', color: medalColor(pos) }}>
                    {pos === 1 ? '🥇' : pos === 2 ? '🥈' : '🥉'}
                  </span>
                  <h3 style={{ fontSize: '1rem', margin: '6px 0 4px', borderBottom: 'none' }}>{game.nome || '—'}</h3>
                  <p style={{ color: '#7fd67f', fontSize: '0.8rem', margin: '0 0 4px' }}>
                    {game.jogadoresOnline != null ? game.jogadoresOnline : 0} online
                  </p>
                  <p style={{ color: '#999', fontSize: '0.72rem', margin: '0 0 6px' }}>
                    Pico recorde: {(game.picoJogadoresOnline ?? 0).toLocaleString('pt-BR')}
                  </p>
                  <span style={{ fontSize: '0.8rem', fontWeight: '600', color: '#9ec8ff' }}>
                    Popularidade: {formatScore(game.scorePopularidade)}
                  </span>
                </div>
              </div>
            );
          })}
        </div>

        <div style={{ overflowX: 'auto', marginBottom: '40px' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid #444', textAlign: 'left' }}>
                <th style={{ padding: '10px 8px', color: '#999', fontWeight: '600' }}>#</th>
                <th style={{ padding: '10px 8px', color: '#999', fontWeight: '600' }}>Jogo</th>
                <th style={{ padding: '10px 8px', color: '#999', fontWeight: '600', textAlign: 'right' }}>Online</th>
                <th style={{ padding: '10px 8px', color: '#999', fontWeight: '600', textAlign: 'right' }}>Pico</th>
                <th style={{ padding: '10px 8px', color: '#999', fontWeight: '600', textAlign: 'right' }}>Popularidade</th>
                <th style={{ padding: '10px 8px', color: '#999', fontWeight: '600' }}>Modo</th>
                <th style={{ padding: '10px 8px', color: '#999', fontWeight: '600' }}>Status</th>
              </tr>
            </thead>
            <tbody>
              {ranking.map((game) => {
                const pos = game.posicao ?? 0;
                return (
                  <tr key={game.id} style={{ borderBottom: '1px solid #333', transition: 'background 0.15s' }}
                    onMouseEnter={e => { e.currentTarget.style.background = '#2a2a2a'; }}
                    onMouseLeave={e => { e.currentTarget.style.background = 'transparent'; }}
                  >
                    <td style={{ padding: '10px 8px', fontWeight: '700', color: medalColor(pos), fontSize: '1rem' }}>
                      {pos <= 3 ? (pos === 1 ? '🥇' : pos === 2 ? '🥈' : '🥉') : pos}
                    </td>
                    <td style={{ padding: '10px 8px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <img
                          src={game.capaDisponivel ? `/mongo/arquivos/${game.id}/imagem` : '/gamehub.png'}
                          alt=""
                          style={{ width: '48px', height: '28px', objectFit: 'cover', borderRadius: '4px', background: '#222' }}
                          onError={(e) => { e.currentTarget.src = '/gamehub.png'; }}
                        />
                        <span style={{ fontWeight: '600', color: '#f7f7f7' }}>{game.nome || '—'}</span>
                      </div>
                    </td>
                    <td style={{ padding: '10px 8px', textAlign: 'right', color: '#9f9', fontWeight: '600' }}>
                      {(game.jogadoresOnline ?? 0).toLocaleString('pt-BR')}
                    </td>
                    <td style={{ padding: '10px 8px', textAlign: 'right', color: '#ccc' }}>
                      {(game.picoJogadoresOnline ?? 0).toLocaleString('pt-BR')}
                    </td>
                    <td style={{ padding: '10px 8px', textAlign: 'right', color: '#9ec8ff', fontWeight: '600' }}>
                      {formatScore(game.scorePopularidade)}
                    </td>
                    <td style={{ padding: '10px 8px', color: '#aaa' }}>{game.modoJogo || '—'}</td>
                    <td style={{ padding: '10px 8px' }}>
                      <span style={{ fontSize: '0.7rem', padding: '2px 8px', background: '#333', borderRadius: '10px', color: '#ccc' }}>
                        {game.status || '—'}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
      )}

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

export default Rankings;
