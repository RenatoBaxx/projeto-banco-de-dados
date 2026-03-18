import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleLogin = (e) => {
    e.preventDefault();
    if (!email || !password) return alert('Preencha os campos!');

    console.log('Logging in with', email, password);
    navigate('/dashboard');
  };

  return (
    <div className="app-container" style={{ minHeight: '80vh' }}>
      <main className="grid">
        <section className="login-section">
          <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
            <p style={{ fontSize: '2rem', fontWeight: 'bold', margin: '0 0 0.75rem 0' }}>
              GameHUB Login
            </p>
            <img
              src="/gamehub.png"
              alt="GameHUB"
              style={{ display: 'block', width: '100%', maxWidth: '240px', margin: '0 auto' }}
            />
          </div>
          <form onSubmit={handleLogin}>
            <div className="form-group">
              <label>Email</label>
              <input
                type="email"
                className="form-control"
                placeholder="seu@email.com"
                value={email}
                onChange={e => setEmail(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label>Senha</label>
              <input
                type="password"
                className="form-control"
                placeholder="••••••••"
                value={password}
                onChange={e => setPassword(e.target.value)}
              />
            </div>
            <button type="submit" className="btn btn-primary" style={{ marginTop: '1.5rem' }}>Entrar</button>

            <div style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.875rem' }}>
              <span>Não tem uma conta? </span>
              <button
                type="button"
                onClick={() => navigate('/register')}
                style={{ background: 'none', border: 'none', color: '#007bff', textDecoration: 'underline' }}
              >
                Cadastre sua empresa
              </button>
            </div>
          </form>
        </section>
      </main>
    </div>
  );
}

export default Login;
