import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';

const MOCK_GAMES = [
  { id: 1, name: 'Counter Strike', studio: 'Nova Games', genre: 'Ação', players: 12400, platforms: ['Steam', 'Epic Games'], os: ['Windows', 'Linux'], mode: 'Multiplayer', status: 'Aprovado', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/730/header.jpg' },
  { id: 2, name: 'Dungeon Depths', studio: 'PixelForge', genre: 'RPG', players: 8300, platforms: ['Steam'], os: ['Windows', 'macOS'], mode: 'Singleplayer', status: 'Aprovado', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/1245620/header.jpg' },
  { id: 3, name: 'Speed Circuit', studio: 'TurboSoft', genre: 'Corrida', players: 25600, platforms: ['Steam', 'PlayStation Store', 'Xbox Store'], os: ['Windows'], mode: 'Multiplayer', status: 'Aprovado', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/1551360/header.jpg' },
  { id: 4, name: 'Farm Valley', studio: 'CozyDev', genre: 'Simulação', players: 41200, platforms: ['Steam', 'Google Play'], os: ['Windows', 'Android'], mode: 'Singleplayer', status: 'Aprovado', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/413150/header.jpg' },
  { id: 5, name: 'Shadow Ops', studio: 'DarkByte', genre: 'FPS', players: 53800, platforms: ['Steam', 'Epic Games', 'Xbox Store'], os: ['Windows'], mode: 'Multiplayer', status: 'Aprovado', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/1938090/header.jpg' },
  { id: 6, name: 'Puzzle Mind', studio: 'BrainBox', genre: 'Puzzle', players: 6100, platforms: ['Steam', 'Google Play', 'App Store'], os: ['Windows', 'Android', 'iOS'], mode: 'Singleplayer', status: 'Aprovado', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/105600/header.jpg' },
  { id: 7, name: 'Galaxy Builder', studio: 'StarForge', genre: 'Estratégia', players: 18900, platforms: ['Steam'], os: ['Windows', 'macOS', 'Linux'], mode: 'Ambos', status: 'Aprovado', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/281990/header.jpg' },
  { id: 8, name: 'Arena Legends', studio: 'Nova Games', genre: 'MOBA', players: 87500, platforms: ['Steam', 'Epic Games'], os: ['Windows'], mode: 'Multiplayer', status: 'Aprovado', cover: 'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/570/header.jpg' },
];

const GENRES = ['Todos', 'Ação', 'RPG', 'Corrida', 'Simulação', 'FPS', 'Puzzle', 'Estratégia', 'MOBA'];

function Jogos() {
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [genreFilter, setGenreFilter] = useState('Todos');
  const [modeFilter, setModeFilter] = useState('Todos');

  const filtered = MOCK_GAMES.filter(g => {
    const matchSearch = g.name.toLowerCase().includes(search.toLowerCase()) || g.studio.toLowerCase().includes(search.toLowerCase());
    const matchGenre = genreFilter === 'Todos' || g.genre === genreFilter;
    const matchMode = modeFilter === 'Todos' || g.mode === modeFilter;
    return matchSearch && matchGenre && matchMode;
  });

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
          <a href="/#rankings" style={{ color: '#ccc', textDecoration: 'none', fontSize: '0.8rem' }}>Rankings</a>
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

      <header style={{ textAlign: 'center', padding: '40px 20px 20px' }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Catálogo de Jogos</h1>
        <p style={{ color: '#999', fontSize: '0.95rem' }}>Todos os jogos publicados através da plataforma GameHUB</p>
      </header>

      <div style={{ maxWidth: '1100px', margin: '0 auto', width: '100%', padding: '0 20px' }}>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px', alignItems: 'center', marginBottom: '24px' }}>
          <input
            type="text"
            className="form-control"
            placeholder="Buscar por nome ou estúdio..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            style={{ maxWidth: '300px', flex: '1' }}
          />
          <select
            value={genreFilter}
            onChange={e => setGenreFilter(e.target.value)}
            className="form-control"
            style={{ width: 'auto', maxWidth: '160px' }}
          >
            {GENRES.map(g => <option key={g} value={g}>{g}</option>)}
          </select>
          <select
            value={modeFilter}
            onChange={e => setModeFilter(e.target.value)}
            className="form-control"
            style={{ width: 'auto', maxWidth: '160px' }}
          >
            <option value="Todos">Todos os modos</option>
            <option value="Singleplayer">Singleplayer</option>
            <option value="Multiplayer">Multiplayer</option>
            <option value="Ambos">Ambos</option>
          </select>
          <span style={{ color: '#888', fontSize: '0.85rem', marginLeft: 'auto' }}>
            {filtered.length} jogo{filtered.length !== 1 ? 's' : ''} encontrado{filtered.length !== 1 ? 's' : ''}
          </span>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '20px', paddingBottom: '40px' }}>
          {filtered.map(game => (
            <div key={game.id} className="card" style={{ padding: '0', overflow: 'hidden', transition: 'transform 0.2s', cursor: 'pointer' }}>
              <img
                src={game.cover}
                alt={game.name}
                style={{ width: '100%', height: '140px', objectFit: 'cover', display: 'block', background: '#2a2a2a' }}
              />
              {/* Info */}
              <div style={{ padding: '14px' }}>
                <h3 style={{ fontSize: '1rem', margin: '0 0 4px', borderBottom: 'none' }}>{game.name}</h3>
                <p style={{ color: '#999', fontSize: '0.8rem', margin: '0 0 10px' }}>{game.studio}</p>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginBottom: '10px' }}>
                  <span style={{ fontSize: '0.7rem', padding: '2px 8px', background: '#333', borderRadius: '10px', color: '#ccc' }}>{game.genre}</span>
                  <span style={{ fontSize: '0.7rem', padding: '2px 8px', background: '#333', borderRadius: '10px', color: '#ccc' }}>{game.mode}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontSize: '0.8rem', color: '#aaa' }}>{game.players.toLocaleString('pt-BR')} jogadores</span>
                  <div style={{ display: 'flex', gap: '4px' }}>
                    {game.platforms.slice(0, 2).map(p => (
                      <span key={p} style={{ fontSize: '0.6rem', padding: '2px 6px', border: '1px solid #444', borderRadius: '4px', color: '#aaa' }}>{p}</span>
                    ))}
                    {game.platforms.length > 2 && (
                      <span style={{ fontSize: '0.6rem', padding: '2px 6px', border: '1px solid #444', borderRadius: '4px', color: '#aaa' }}>+{game.platforms.length - 2}</span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        {filtered.length === 0 && (
          <div style={{ textAlign: 'center', padding: '60px 20px', color: '#888' }}>
            <p style={{ fontSize: '2rem', marginBottom: '8px' }}>😕</p>
            <p>Nenhum jogo encontrado com esses filtros.</p>
          </div>
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
