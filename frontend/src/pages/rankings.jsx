import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';

const RANKING_DATA = [
  { pos: 1, name: 'Arena Legends', studio: 'Nova Games', players: 87500, genre: 'MOBA', trend: '+12%', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/570/header.jpg' },
  { pos: 2, name: 'Shadow Ops', studio: 'DarkByte', players: 53800, genre: 'FPS', trend: '+8%', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/1938090/header.jpg' },
  { pos: 3, name: 'Farm Valley', studio: 'CozyDev', players: 41200, genre: 'Simulação', trend: '+23%', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/413150/header.jpg' },
  { pos: 4, name: 'Speed Circuit', studio: 'TurboSoft', players: 25600, genre: 'Corrida', trend: '+5%', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/1551360/header.jpg' },
  { pos: 5, name: 'Galaxy Builder', studio: 'StarForge', players: 18900, genre: 'Estratégia', trend: '+15%', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/281990/header.jpg' },
  { pos: 6, name: 'Counter Strike', studio: 'Nova Games', players: 12400, genre: 'Ação', trend: '-2%', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/730/header.jpg' },
  { pos: 7, name: 'Dungeon Depths', studio: 'PixelForge', players: 8300, genre: 'RPG', trend: '+4%', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/1245620/header.jpg' },
  { pos: 8, name: 'Puzzle Mind', studio: 'BrainBox', players: 6100, genre: 'Puzzle', trend: '-1%', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/105600/header.jpg' },
];

function Rankings() {
  const navigate = useNavigate();
  const [period, setPeriod] = useState('year');

  const medalColor = (pos) => {
    if (pos === 1) return '#ffd700';
    if (pos === 2) return '#c0c0c0';
    if (pos === 3) return '#cd7f32';
    return '#666';
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>

      {/* NAVBAR */}
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
        <p style={{ color: '#999', fontSize: '0.95rem' }}>Os jogos mais populares publicados na plataforma GameHUB</p>
      </header>

      <div style={{ display: 'flex', justifyContent: 'center', gap: '8px', padding: '16px 20px 24px' }}>
        {[
          { value: 'week', label: 'Semana' },
          { value: 'month', label: 'Mês' },
          { value: 'year', label: 'Ano' },
          { value: 'all', label: 'Todos os tempos' },
        ].map(p => (
          <button
            key={p.value}
            onClick={() => setPeriod(p.value)}
            style={{
              padding: '6px 16px',
              borderRadius: '20px',
              border: period === p.value ? 'none' : '1px solid #444',
              background: period === p.value ? '#f7f7f7' : '#2a2a2a',
              color: period === p.value ? '#1f1f1f' : '#c9c9c9',
              fontWeight: period === p.value ? '600' : '400',
              fontSize: '0.8rem',
              cursor: 'pointer',
              fontFamily: "'Poppins', sans-serif",
              transition: 'all 0.2s',
            }}
          >
            {p.label}
          </button>
        ))}
      </div>

      <div style={{ maxWidth: '900px', margin: '0 auto', width: '100%', padding: '0 20px' }}>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '20px', marginBottom: '32px', flexWrap: 'wrap' }}>
          {RANKING_DATA.slice(0, 3).map(game => (
            <div key={game.pos} style={{
              flex: '0 1 260px',
              background: '#2a2a2a',
              borderRadius: '10px',
              overflow: 'hidden',
              border: `2px solid ${medalColor(game.pos)}`,
              textAlign: 'center',
            }}>
              <img src={game.cover} alt={game.name} style={{ width: '100%', height: '120px', objectFit: 'cover' }} />
              <div style={{ padding: '14px' }}>
                <span style={{ fontSize: '1.5rem', fontWeight: '700', color: medalColor(game.pos) }}>
                  {game.pos === 1 ? '🥇' : game.pos === 2 ? '🥈' : '🥉'}
                </span>
                <h3 style={{ fontSize: '1rem', margin: '6px 0 2px', borderBottom: 'none' }}>{game.name}</h3>
                <p style={{ color: '#999', fontSize: '0.75rem', margin: '0 0 8px' }}>{game.studio}</p>
                <span style={{ fontSize: '0.85rem', fontWeight: '600', color: '#f7f7f7' }}>
                  Jogadores: {game.players.toLocaleString('pt-BR')}
                </span>
              </div>
            </div>
          ))}
        </div>

        <div style={{ overflowX: 'auto' , marginBottom: '40px' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid #444', textAlign: 'left' }}>
                <th style={{ padding: '10px 12px', color: '#999', fontWeight: '600' }}>#</th>
                <th style={{ padding: '10px 12px', color: '#999', fontWeight: '600' }}>Jogo</th>
                <th style={{ padding: '10px 12px', color: '#999', fontWeight: '600' }}>Estúdio</th>
                <th style={{ padding: '10px 12px', color: '#999', fontWeight: '600' }}>Gênero</th>
                <th style={{ padding: '10px 12px', color: '#999', fontWeight: '600', textAlign: 'right' }}>Jogadores</th>
              </tr>
            </thead>
            <tbody>
              {RANKING_DATA.map(game => (
                <tr key={game.pos} style={{ borderBottom: '1px solid #333', transition: 'background 0.15s' }}
                  onMouseEnter={e => e.currentTarget.style.background = '#2a2a2a'}
                  onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
                >
                  <td style={{ padding: '12px', fontWeight: '700', color: medalColor(game.pos), fontSize: '1rem' }}>
                    {game.pos <= 3 ? (game.pos === 1 ? '🥇' : game.pos === 2 ? '🥈' : '🥉') : game.pos}
                  </td>
                  <td style={{ padding: '12px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <img src={game.cover} alt={game.name} style={{ width: '48px', height: '28px', objectFit: 'cover', borderRadius: '4px' }} />
                      <span style={{ fontWeight: '600', color: '#f7f7f7' }}>{game.name}</span>
                    </div>
                  </td>
                  <td style={{ padding: '12px', color: '#aaa' }}>{game.studio}</td>
                  <td style={{ padding: '12px' }}>
                    <span style={{ fontSize: '0.7rem', padding: '2px 8px', background: '#333', borderRadius: '10px', color: '#ccc' }}>{game.genre}</span>
                  </td>
                  <td style={{ padding: '12px', textAlign: 'right', fontWeight: '600' }}>
                    {game.players.toLocaleString('pt-BR')}
                  </td> 
                </tr>
              ))}
            </tbody>
          </table>
        </div>
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

export default Rankings;
