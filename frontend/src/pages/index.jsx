import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';

function Index() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const raw = localStorage.getItem('token');
    if (!raw) return;
    try {
      const { access_token } = JSON.parse(raw);
      if (!access_token) return;
      fetch('/api/auth/me', { headers: { Authorization: 'Bearer ' + access_token } })
        .then(r => { if (!r.ok) throw new Error(); return r.json(); })
        .then(data => { if (data.email) setUser(data); })
        .catch(() => {});
    } catch {}
  }, []);

  useEffect(() => {
    const handleClick = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) setDropdownOpen(false);
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', }}>

      <nav style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '15px 30px',
        backgroundColor: '#333',
        color: 'white'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          <div style={{ fontSize: '1.2rem' }}> GameHUB</div>
          <a
            href="/jogos"
            style={{
              color: '#ccc',
              textDecoration: 'none',
              fontSize: '0.8rem',
              cursor: 'pointer',
            }}
          >
            Jogos
          </a>
                    <a
            href="/rankings"
            style={{
              color: '#ccc',
              textDecoration: 'none',
              fontSize: '0.8rem',
              cursor: 'pointer',
            }}
          >
            Rankings
          </a>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          {user ? (
            <div ref={dropdownRef} style={{ position: 'relative' }}>
              <button
                onClick={() => setDropdownOpen(!dropdownOpen)}
                style={{
                  background: 'transparent',
                  color: 'white',
                  border: '1px solid #666',
                  padding: '8px 15px',
                  borderRadius: '5px',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px'
                }}
              >
                {user.email.split('@')[0]}
                <span style={{ fontSize: '0.6rem' }}>▼</span>
              </button>
              {dropdownOpen && (
                <div style={{
                  position: 'absolute',
                  top: '100%',
                  right: 0,
                  marginTop: '5px',
                  background: '#444',
                  borderRadius: '5px',
                  minWidth: '150px',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.3)',
                  zIndex: 100
                }}>
                  <button
                    onClick={() => navigate('/dashboard')}
                    style={{ display: 'block', width: '100%', padding: '10px 15px', background: 'transparent', color: 'white', border: 'none', textAlign: 'left', cursor: 'pointer' }}
                  >
                    Dashboard
                  </button>
                  <button
                    onClick={() => { localStorage.removeItem('token'); setUser(null); setDropdownOpen(false); }}
                    style={{ display: 'block', width: '100%', padding: '10px 15px', background: 'transparent', color: '#ff6b6b', border: 'none', textAlign: 'left', cursor: 'pointer' }}
                  >
                    Sair
                  </button>
                </div>
              )}
            </div>
          ) : (
            <>
              <button
                onClick={() => navigate('/login')}
                style={{
                  background: 'transparent',
                  color: 'white',
                  border: '1px solid white',
                  padding: '8px 15px',
                  marginRight: '10px',
                  borderRadius: '5px',
                  cursor: 'pointer'
                }}
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
            </>
          )}
        </div>
      </nav>

      <header style={{
        textAlign: 'center',
        padding: '60px 20px',
      }}>
        <h1 style={{ fontSize: '2.5rem', marginBottom: '10px' }}>Bem-vindo ao GameHUB</h1>
        <p style={{ fontSize: '1.2rem', color: '#666', maxWidth: '600px', margin: '0 auto' }}>
          A Plataforma definitiva para gerenciar o envio e publicação de jogos nas principais plataformas digitais.
        </p>
      </header>

      <main style={{ flex: 1, padding: '40px 20px', maxWidth: '1000px', margin: '0 auto', width: '100%' }}>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '30px' }}>

          <div className="card" style={{ textAlign: 'center' }}>
            <h3 style={{ borderBottom: 'none', justifyContent: 'center' }}>Integração Simplificada</h3>
            <p style={{ color: '#555' }}>
              Conecte-se facilmente com a Steam, Epic Games e outras plataformas através de uma única interface de gerenciamento.
            </p>
          </div>

          <div className="card" style={{ textAlign: 'center' }}>
            <h3 style={{ borderBottom: 'none', justifyContent: 'center' }}>Acompanhamento em Tempo Real</h3>
            <p style={{ color: '#555' }}>
              Monitore o status exato de cada upload de jogo. Saiba imediatamente quando uma build for aprovada ou apresentar problemas.
            </p>
          </div>

          <div className="card" style={{ textAlign: 'center' }}>
            <h3 style={{ borderBottom: 'none', justifyContent: 'center' }}>Gestão de Múltiplas Lojas</h3>
            <p style={{ color: '#555' }}>
              Dispare atualizações simultâneas para diferentes vitrines sem precisar refazer os pacotes individualmente.
            </p>
          </div>

        </div>
        <div style={{ marginTop: '60px', textAlign: 'center' }}> 
          <h3 style={{ borderBottom: 'none', marginBottom: '24px', color: '#999', fontSize: '1rem', fontWeight: '400' }}>Empresas que utilizam nossa plataforma</h3>
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexWrap: 'wrap', gap: '40px', opacity: 0.7 }}>
            <img src="/ubisoft-logo.svg" alt="Ubisoft" style={{ height: '40px', objectFit: 'contain', filter: 'grayscale(100%) brightness(1.5)' }} />
            <img src="/riot-logo.png" alt="Riot Games" style={{ height: '40px', objectFit: 'contain', filter: 'grayscale(100%) brightness(1.5)' }} />
            <img src="/nintendo-logo.png" alt="Nintendo" style={{ height: '40px', objectFit: 'contain', filter: 'grayscale(100%) brightness(1.5)' }} />
            <img src="/Capcom_logo.png" alt="Capcom" style={{ height: '40px', objectFit: 'contain', filter: 'grayscale(100%) brightness(1.5)' }} />
            <img src="/Konami-Logo.png" alt="Konami" style={{ height: '40px', objectFit: 'contain', filter: 'grayscale(100%) brightness(1.5)' }} />
            <img src="/ea-logo.png" alt="EA" style={{ height: '40px', objectFit: 'contain', filter: 'grayscale(100%) brightness(1.5)' }} />
          </div>
        </div>  
      </main>   

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

export default Index;
