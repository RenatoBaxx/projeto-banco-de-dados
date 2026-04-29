import { useNavigate } from 'react-router-dom';
import '../App.css';

function Index() {
  const navigate = useNavigate();

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
        <div style={{ fontSize: '1.2rem' }}> GameHUB</div>
        <div>
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
        </div>
      </nav>

      <header style={{
        textAlign: 'center',
        padding: '60px 20px',
      }}>
        <h1 style={{ fontSize: '2.5rem', marginBottom: '10px' }}>Bem-vindo ao GameHUB</h1>
        <p style={{ fontSize: '1.2rem', color: '#666', maxWidth: '600px', margin: '0 auto' }}>
          A Startup definitiva para gerenciar o envio e publicação de jogos nas principais plataformas digitais.
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
