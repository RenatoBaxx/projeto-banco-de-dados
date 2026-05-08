import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';

function formatTamanho(bytes) {
  if (bytes == null || bytes === 0) return '—';
  const mb = bytes / (1024 * 1024);
  if (mb < 0.01) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${mb.toFixed(2)} MB`;
}

function Rankings() {
  const navigate = useNavigate();
  const [ranking, setRanking] = useState([]);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState(null);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      setLoading(true);
      setErro(null);
      try {
        const res = await fetch('/catalog/ranking');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        if (!cancelado) setRanking(Array.isArray(data) ? data : []);
      } catch (e) {
        if (!cancelado) {
          setErro('Não foi possível carregar o ranking. Confira se o backend e o Redis estão no ar.');
          setRanking([]);
        }
      } finally {
        if (!cancelado) setLoading(false);
      }
    })();
    return () => { cancelado = true; };
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

      <header style={{ textAlign: 'center', padding: '40px 20px 10px' }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Ranking de Jogos</h1>
      </header>

      {loading && (
        <p style={{ textAlign: 'center', color: '#999', padding: '24px' }}>Carregando ranking…</p>
      )}
      {erro && !loading && (
        <p style={{ textAlign: 'center', color: '#e88', padding: '16px 20px' }}>{erro}</p>
      )}

      {!loading && !erro && ranking.length === 0 && (
        <p style={{ textAlign: 'center', color: '#999', padding: '24px', maxWidth: '520px', margin: '0 auto' }}>
          Nenhum jogo no catálogo. Publique jogos pelo dashboard para aparecerem aqui após reiniciar o servidor.
        </p>
      )}

      {!loading && ranking.length > 0 && (
      <div style={{ maxWidth: '960px', margin: '0 auto', width: '100%', padding: '0 20px' }}>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '20px', marginBottom: '32px', flexWrap: 'wrap' }}>
          {topThree.map((game) => (
            <div key={game.id} style={{
              flex: '0 1 260px',
              background: '#2a2a2a',
              borderRadius: '10px',
              overflow: 'hidden',
              border: `2px solid ${medalColor(game.posicao)}`,
              textAlign: 'center',
            }}>
              <img
                src={game.capaDisponivel ? `/mongo/arquivos/${game.id}/imagem` : '/gamehub.png'}
                alt=""
                style={{ width: '100%', height: '120px', objectFit: 'cover', background: '#1a1a1a' }}
                onError={(e) => { e.currentTarget.src = '/gamehub.png'; }}
              />
              <div style={{ padding: '14px' }}>
                <span style={{ fontSize: '1.5rem', fontWeight: '700', color: medalColor(game.posicao) }}>
                  {game.posicao === 1 ? '🥇' : game.posicao === 2 ? '🥈' : '🥉'}
                </span>
                <h3 style={{ fontSize: '1rem', margin: '6px 0 2px', borderBottom: 'none' }}>{game.nome || '—'}</h3>
                <p style={{ color: '#999', fontSize: '0.75rem', margin: '0 0 8px' }}>{game.modoJogo || '—'}</p>
                <span style={{ fontSize: '0.85rem', fontWeight: '600', color: '#f7f7f7' }}>
                  {game.preco || '—'}
                </span>
              </div>
            </div>
          ))}
        </div>

        <div style={{ overflowX: 'auto', marginBottom: '40px' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid #444', textAlign: 'left' }}>
                <th style={{ padding: '10px 12px', color: '#999', fontWeight: '600' }}>#</th>
                <th style={{ padding: '10px 12px', color: '#999', fontWeight: '600' }}>Jogo</th>
                <th style={{ padding: '10px 12px', color: '#999', fontWeight: '600' }}>Modo</th>
                <th style={{ padding: '10px 12px', color: '#999', fontWeight: '600' }}>Preço</th>
                <th style={{ padding: '10px 12px', color: '#999', fontWeight: '600' }}>Plataformas</th>
                <th style={{ padding: '10px 12px', color: '#999', fontWeight: '600' }}>Status</th>
                <th style={{ padding: '10px 12px', color: '#999', fontWeight: '600', textAlign: 'right' }}>Pacote</th>
              </tr>
            </thead>
            <tbody>
              {ranking.map((game) => (
                <tr key={game.id} style={{ borderBottom: '1px solid #333', transition: 'background 0.15s' }}
                  onMouseEnter={e => { e.currentTarget.style.background = '#2a2a2a'; }}
                  onMouseLeave={e => { e.currentTarget.style.background = 'transparent'; }}
                >
                  <td style={{ padding: '12px', fontWeight: '700', color: medalColor(game.posicao), fontSize: '1rem' }}>
                    {game.posicao <= 3 ? (game.posicao === 1 ? '🥇' : game.posicao === 2 ? '🥈' : '🥉') : game.posicao}
                  </td>
                  <td style={{ padding: '12px' }}>
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
                  <td style={{ padding: '12px', color: '#aaa' }}>{game.modoJogo || '—'}</td>
                  <td style={{ padding: '12px' }}>{game.preco || '—'}</td>
                  <td style={{ padding: '12px', maxWidth: '220px' }}>
                    <span style={{ fontSize: '0.7rem', color: '#ccc' }}>
                      {(game.plataformasPublicacao || []).join(', ') || '—'}
                    </span>
                  </td>
                  <td style={{ padding: '12px' }}>
                    <span style={{ fontSize: '0.7rem', padding: '2px 8px', background: '#333', borderRadius: '10px', color: '#ccc' }}>
                      {game.status || '—'}
                    </span>
                  </td>
                  <td style={{ padding: '12px', textAlign: 'right', fontWeight: '600' }}>
                    {formatTamanho(game.arquivoTamanhoBytes)}
                  </td>
                </tr>
              ))}
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
