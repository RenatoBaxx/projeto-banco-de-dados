import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';

function Dashboard() {
  const [createData, setCreateData] = useState({ gameId: '', loja: '', status: 'PENDENTE' });
  const [searchId, setSearchId] = useState('');
  const [searchResult, setSearchResult] = useState(null);
  const [searchError, setSearchError] = useState(null);

  const [updateData, setUpdateData] = useState({ id: '', status: '' });
  const [deleteId, setDeleteId] = useState('');

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!createData.gameId || !createData.loja) return alert("Preencha Game ID e Loja");

    try {
      const res = await fetch("/uploads", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(createData)
      });
      if (res.ok) {
        alert("Upload criado com sucesso!");
        setCreateData({ gameId: '', loja: '', status: 'PENDENTE' });
      } else {
        alert("Erro ao criar upload.");
      }
    } catch (err) {
      alert("Erro de conexão.");
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchId) return;
    setSearchError(null);
    setSearchResult(null);

    try {
      const response = await fetch("/uploads/" + searchId);
      if (response.status === 404) {
        setSearchError("Upload não encontrado");
        return;
      }
      if (response.ok) {
        const data = await response.json();
        setSearchResult(data);
      } else {
        setSearchError("Erro ao buscar upload.");
      }
    } catch (err) {
      setSearchError("Erro de conexão.");
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    if (!updateData.id || !updateData.status) return alert("Preencha ID e Novo Status");

    try {
      const res = await fetch(`/uploads/${updateData.id}?status=${updateData.status}`, {
        method: "PUT"
      });
      if (res.ok) {
        alert("Status atualizado com sucesso!");
        setUpdateData({ id: '', status: '' });
      } else {
        alert("Erro ao atualizar status.");
      }
    } catch (err) {
      alert("Erro de conexão.");
    }
  };

  const handleDelete = async (e) => {
    e.preventDefault();
    if (!deleteId) return;

    if (!confirm(`Tem certeza que deseja deletar o upload ${deleteId}?`)) return;

    try {
      const res = await fetch("/uploads/" + deleteId, {
        method: "DELETE"
      });
      if (res.ok) {
        alert("Upload deletado com sucesso!");
        setDeleteId('');
      } else {
        alert("Erro ao deletar (Pode não existir).");
      }
    } catch (err) {
      alert("Erro de conexão.");
    }
  };

  const navigate = useNavigate();

  return (
    <div className="app-container">
      <header className="header" style={{ position: 'relative' }}>
        <button
          onClick={() => navigate('/')}
          className="btn btn-danger"
          style={{ width: 'auto', position: 'absolute', right: '20px', top: '20px' }}
        >
          Sair
        </button>
        <h1>Gerenciador de Uploads</h1>
        <p>Dashboard Administrativo para Upload de Jogos</p>
      </header>

      <main className="grid">

        <section className="card">
          <h2>Criar Upload <span className="status-badge">POST</span></h2>
          <form onSubmit={handleCreate}>
            <div className="form-group">
              <label>Game ID</label>
              <input
                type="text"
                className="form-control"
                placeholder="Ex: 12345"
                value={createData.gameId}
                onChange={e => setCreateData({ ...createData, gameId: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>Loja</label>
              <input
                type="text"
                className="form-control"
                placeholder="Ex: Steam"
                value={createData.loja}
                onChange={e => setCreateData({ ...createData, loja: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>Status</label>
              <input
                type="text"
                className="form-control"
                value={createData.status}
                onChange={e => setCreateData({ ...createData, status: e.target.value })}
              />
            </div>
            <button type="submit" className="btn btn-primary">Criar Upload</button>
          </form>
        </section>

        <section className="card">
          <h2>Consultar Upload <span className="status-badge">GET</span></h2>
          <form onSubmit={handleSearch}>
            <div className="form-group">
              <label>Game ID</label>
              <input
                type="text"
                className="form-control"
                placeholder="ID para busca..."
                value={searchId}
                onChange={e => setSearchId(e.target.value)}
              />
            </div>
            <button type="submit" className="btn btn-primary" disabled={!searchId}>Buscar</button>
          </form>

          {searchResult && (
            <div className="result-box">
              {JSON.stringify(searchResult, null, 2)}
            </div>
          )}
          {searchError && (
            <div className="result-box error-box">
              {searchError}
            </div>
          )}
        </section>

        {/* UPDATE CARD */}
        <section className="card">
          <h2>Atualizar Status <span className="status-badge">PUT</span></h2>
          <form onSubmit={handleUpdate}>
            <div className="form-group">
              <label>Game ID</label>
              <input
                type="text"
                className="form-control"
                placeholder="ID..."
                value={updateData.id}
                onChange={e => setUpdateData({ ...updateData, id: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>Novo Status</label>
              <input
                type="text"
                className="form-control"
                placeholder="Ex: CONCLUIDO"
                value={updateData.status}
                onChange={e => setUpdateData({ ...updateData, status: e.target.value })}
              />
            </div>
            <button type="submit" className="btn btn-primary">Atualizar</button>
          </form>
        </section>

        {/* DELETE CARD */}
        <section className="card">
          <h2>Deletar Upload <span className="status-badge">DEL</span></h2>
          <form onSubmit={handleDelete}>
            <div className="form-group">
              <label>Game ID</label>
              <input
                type="text"
                className="form-control"
                placeholder="ID para deletar..."
                value={deleteId}
                onChange={e => setDeleteId(e.target.value)}
              />
            </div>
            <button type="submit" className="btn btn-danger" disabled={!deleteId}>Deletar Upload</button>
          </form>
        </section>

      </main>
    </div>
  )
}

export default Dashboard;
