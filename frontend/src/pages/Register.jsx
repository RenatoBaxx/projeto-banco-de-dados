import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';

function Register() {
  const [formData, setFormData] = useState({
    razaoSocial: '',
    cnpj: '',
    email: '',
    password: ''
  });
  const navigate = useNavigate();

  const handleRegister = (e) => {
    e.preventDefault();
    if (!formData.razaoSocial || !formData.cnpj || !formData.email || !formData.password) {
      return alert('Preencha todos os campos!');
    }

    console.log('Registering company:', formData);
    alert('Empresa cadastrada com sucesso!');
    navigate('/');
  };

  return (
    <div className="app-container" style={{ minHeight: '80vh' }}>
      <main className="grid">
        <section className="card">
          <h2 style={{ textAlign: 'center' }}>
            <span>
              Cadastrar Empresa
            </span>
          </h2>
          <form onSubmit={handleRegister}>
            <div className="form-group">
              <label>Razão Social</label>
              <input
                type="text"
                className="form-control"
                placeholder="Nome da sua Empresa LTDA"
                value={formData.razaoSocial}
                onChange={e => setFormData({ ...formData, razaoSocial: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>CNPJ</label>
              <input
                type="text"
                className="form-control"
                placeholder="00.000.000/0001-00"
                value={formData.cnpj}
                onChange={e => setFormData({ ...formData, cnpj: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>Email Corporativo</label>
              <input
                type="email"
                className="form-control"
                placeholder="contato@empresa.com.br"
                value={formData.email}
                onChange={e => setFormData({ ...formData, email: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>Senha de Acesso</label>
              <input
                type="password"
                className="form-control"
                placeholder="••••••••"
                value={formData.password}
                onChange={e => setFormData({ ...formData, password: e.target.value })}
              />
            </div>
            <button type="submit" className="btn btn-primary" style={{ marginTop: '1.5rem' }}>Cadastrar</button>

            <div style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.875rem' }}>
              <span>Já possui cadastro? </span>
              <button
                type="button"
                onClick={() => navigate('/')}
                style={{ background: 'none', border: 'none', color: '#007bff', textDecoration: 'underline' }}
              >
                Voltar ao Login
              </button>
            </div>
          </form>
        </section>
      </main>
    </div>
  );
}

export default Register;
