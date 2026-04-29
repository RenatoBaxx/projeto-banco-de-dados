import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';

const OS_OPTIONS = ['Windows', 'macOS', 'Linux', 'Android', 'iOS'];
const PLATFORM_OPTIONS = ['Steam', 'Epic Games', 'GOG', 'PlayStation Store', 'Xbox Store', 'Nintendo eShop', 'Google Play', 'App Store'];

function Dashboard() {
  const [activeSection, setActiveSection] = useState('create');
  const [pubStep, setPubStep] = useState(1);
  const [pubData, setPubData] = useState({
    name: '',
    description: '',
    price: '',
    os: [],
    mode: '',
    platforms: [],
    file: null,
  });
  const [pubSubmitted, setPubSubmitted] = useState(false);
  const fileInputRef = useRef(null);

  const handleOsToggle = (os) => {
    setPubData(prev => ({
      ...prev,
      os: prev.os.includes(os) ? prev.os.filter(o => o !== os) : [...prev.os, os]
    }));
  };

  const handlePlatformToggle = (p) => {
    setPubData(prev => ({
      ...prev,
      platforms: prev.platforms.includes(p) ? prev.platforms.filter(x => x !== p) : [...prev.platforms, p]
    }));
  };

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file && !file.name.endsWith('.zip')) {
      alert('Por favor, selecione um arquivo .zip');
      e.target.value = '';
      return;
    }
    setPubData(prev => ({ ...prev, file }));
  };

  const canAdvance = () => {
    switch (pubStep) {
      case 1: return pubData.name.trim() !== '';
      case 2: return pubData.description.trim() !== '' && pubData.price.trim() !== '' && pubData.os.length > 0 && pubData.mode !== '' && pubData.platforms.length > 0;
      case 3: return pubData.file !== null;
      default: return false;
    }
  };

  const handlePublish = async () => {
    setPubSubmitted(true);
  };

  const resetPublish = () => {
    setPubStep(1);
    setPubData({ name: '', description: '', price: '', os: [], mode: '', platforms: [], file: null });
    setPubSubmitted(false);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const [allUploads, setAllUploads] = useState([]);
  const [uploadsLoading, setUploadsLoading] = useState(false);
  const [filterText, setFilterText] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [filterLoja, setFilterLoja] = useState('');

  const fetchAllUploads = async () => {
    setUploadsLoading(true);
    try {
      const res = await fetch('/uploads');
      if (res.ok) {
        const data = await res.json();
        setAllUploads(data);
      }
    } catch (err) {
      console.error('Erro ao buscar uploads:', err);
    } finally {
      setUploadsLoading(false);
    }
  };

  const filteredUploads = allUploads.filter(u => {
    const matchText = !filterText || (u.gameId && u.gameId.toLowerCase().includes(filterText.toLowerCase()));
    const matchStatus = !filterStatus || u.status === filterStatus;
    const matchLoja = !filterLoja || u.loja === filterLoja;
    return matchText && matchStatus && matchLoja;
  });

  const uniqueStatuses = [...new Set(allUploads.map(u => u.status).filter(Boolean))];
  const uniqueLojas = [...new Set(allUploads.map(u => u.loja).filter(Boolean))];

  const [updateData, setUpdateData] = useState({ id: '', status: '' });
  const [deleteId, setDeleteId] = useState('');

  const [settingsData, setSettingsData] = useState({
    nomeEmpresa: '',
    cnpj: '',
    senhaAtual: '',
    senhaNova: '',
    confirmaSenha: '',
  });
  const [settingsSaved, setSettingsSaved] = useState(false);

  const handleSettingsSave = (e) => {
    e.preventDefault();
    if (settingsData.senhaNova && settingsData.senhaNova !== settingsData.confirmaSenha) {
      return alert('A nova senha e a confirmação não coincidem.');
    }
    if (settingsData.senhaNova && !settingsData.senhaAtual) {
      return alert('Informe a senha atual para alterar a senha.');
    }
    console.log('Salvando configurações:', settingsData);
    setSettingsSaved(true);
    setTimeout(() => setSettingsSaved(false), 3000);
    setSettingsData(prev => ({ ...prev, senhaAtual: '', senhaNova: '', confirmaSenha: '' }));
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
    <div className="dashboard-layout">
      <aside className="sidebar">
        <div className="sidebar-logo">
          <img src="/gamehub.png" alt="GameHUB" className="sidebar-logo-img" />
          <span className="sidebar-brand">GameHUB</span>
        </div>

        <nav className="sidebar-nav">
          <button
            className={`sidebar-link ${activeSection === 'create' ? 'active' : ''}`}
            onClick={() => setActiveSection('create')}
          >
            <span className="sidebar-icon">＋</span>
            Publicar
          </button>
          <button
            className={`sidebar-link ${activeSection === 'search' ? 'active' : ''}`}
            onClick={() => setActiveSection('search')}
          >
            <span className="sidebar-icon">🔍</span>
            Ver Publicados
          </button>
          <button
            className={`sidebar-link ${activeSection === 'settings' ? 'active' : ''}`}
            onClick={() => setActiveSection('settings')}
          >
            <span className="sidebar-icon">⚙️</span>
            Configurações
          </button>
        </nav>
        

        <div className="sidebar-footer">
          <button onClick={() => navigate('/')} className="sidebar-link sidebar-logout">
            <span className="sidebar-icon">⬅</span>
            Sair
          </button>
        </div>
      </aside>

      <div className="dashboard-main">
        <header className="dashboard-header">
          <h1>Gerenciador de Uploads</h1>
          <p>Dashboard Administrativo para Upload de Jogos</p>
        </header>

        <div className="dashboard-content">

          {activeSection === 'create' && (
            <section className="card publish-wizard">
              {!pubSubmitted ? (
                <>
                  <div className="wizard-steps">
                    {[1, 2, 3].map(s => (
                      <div key={s} className={`wizard-step-dot ${pubStep === s ? 'active' : ''} ${pubStep > s ? 'done' : ''}`}>
                        {pubStep > s ? '✓' : s}
                      </div>
                    ))}
                  </div>

                  {pubStep === 1 && (
                    <div className="wizard-body">
                      <h2>Nome do Jogo</h2>
                      <p className="wizard-hint">Escolha um nome marcante para o seu jogo.</p>
                      <div className="form-group" style={{ width: '100%' }}>
                        <label>Nome</label>
                        <input
                          type="text"
                          className="form-control"
                          placeholder="Ex: Meu Super Jogo"
                          value={pubData.name}
                          onChange={e => setPubData({ ...pubData, name: e.target.value })}
                        />
                      </div>
                    </div>
                  )}

                  {pubStep === 2 && (
                    <div className="wizard-body">
                      <h2>Detalhes do Jogo</h2>
                      <p className="wizard-hint">Preencha as informações sobre o seu jogo.</p>

                      <div className="form-group" style={{ width: '100%' }}>
                        <label>Descrição</label>
                        <textarea
                          className="form-control"
                          placeholder="Descreva seu jogo..."
                          rows={4}
                          value={pubData.description}
                          onChange={e => setPubData({ ...pubData, description: e.target.value })}
                          style={{ resize: 'vertical' }}
                        />
                      </div>

                      <div className="form-group" style={{ width: '100%' }}>
                        <label>Preço (R$)</label>
                        <input
                          type="text"
                          className="form-control"
                          placeholder="Ex: 49.90 ou Gratuito"
                          value={pubData.price}
                          onChange={e => setPubData({ ...pubData, price: e.target.value })}
                        />
                      </div>

                      <div className="form-group" style={{ width: '100%' }}>
                        <label>Sistemas Operacionais</label>
                        <div className="chip-group">
                          {OS_OPTIONS.map(os => (
                            <button
                              key={os}
                              type="button"
                              className={`chip ${pubData.os.includes(os) ? 'chip-active' : ''}`}
                              onClick={() => handleOsToggle(os)}
                            >
                              {os}
                            </button>
                          ))}
                        </div>
                      </div>

                      <div className="form-group" style={{ width: '100%' }}>
                        <label>Modo de Jogo</label>
                        <div className="chip-group">
                          {['Singleplayer', 'Multiplayer', 'Ambos'].map(m => (
                            <button
                              key={m}
                              type="button"
                              className={`chip ${pubData.mode === m ? 'chip-active' : ''}`}
                              onClick={() => setPubData({ ...pubData, mode: m })}
                            >
                              {m}
                            </button>
                          ))}
                        </div>
                      </div>

                      <div className="form-group" style={{ width: '100%' }}>
                        <label>Plataformas de Publicação</label>
                        <div className="chip-group">
                          {PLATFORM_OPTIONS.map(p => (
                            <button
                              key={p}
                              type="button"
                              className={`chip ${pubData.platforms.includes(p) ? 'chip-active' : ''}`}
                              onClick={() => handlePlatformToggle(p)}
                            >
                              {p}
                            </button>
                          ))}
                        </div>
                      </div>
                    </div>
                  )}

                  {pubStep === 3 && (
                    <div className="wizard-body">
                      <h2>Enviar Arquivo</h2>
                      <p className="wizard-hint">Selecione o arquivo <strong>.zip</strong> do seu jogo.</p>

                      <div
                        className="upload-dropzone"
                        onClick={() => fileInputRef.current?.click()}
                      >
                        {pubData.file ? (
                          <>
                            <span className="upload-icon">📦</span>
                            <span className="upload-filename">{pubData.file.name}</span>
                            <span className="upload-size">{(pubData.file.size / (1024 * 1024)).toFixed(2)} MB</span>
                          </>
                        ) : (
                          <>
                            <span className="upload-icon">📁</span>
                            <span>Clique para selecionar o arquivo .zip</span>
                          </>
                        )}
                      </div>
                      <input
                        ref={fileInputRef}
                        type="file"
                        accept=".zip"
                        style={{ display: 'none' }}
                        onChange={handleFileSelect}
                      />
                    </div>
                  )}

                  <div className="wizard-nav">
                    {pubStep > 1 && (
                      <button
                        type="button"
                        className="btn"
                        onClick={() => setPubStep(pubStep - 1)}
                      >
                        Voltar
                      </button>
                    )}
                    {pubStep < 3 ? (
                      <button
                        type="button"
                        className="btn btn-primary"
                        disabled={!canAdvance()}
                        onClick={() => setPubStep(pubStep + 1)}
                        style={{ marginLeft: 'auto' }}
                      >
                        Próximo
                      </button>
                    ) : (
                      <button
                        type="button"
                        className="btn btn-primary"
                        disabled={!canAdvance()}
                        onClick={handlePublish}
                        style={{ marginLeft: 'auto' }}
                      >
                        Publicar Jogo
                      </button>
                    )}
                  </div>
                </>
              ) : (

                <div className="publish-success">
                  <span className="success-icon">🎮</span>
                  <h2>Jogo Publicado!</h2>
                  <p className="success-game-name">{pubData.name}</p>
                  <div className="success-badge">⏳ Aguardando Aprovação</div>
                  <p className="success-hint">Seu jogo foi enviado e está na fila de análise. Você será notificado quando for aprovado.</p>
                  <button type="button" className="btn btn-primary" onClick={resetPublish} style={{ width: 'auto', marginTop: '1rem' }}>
                    Publicar outro jogo
                  </button>
                </div>
              )}
            </section>
          )}

          {activeSection === 'search' && (
            <section className="card" style={{ maxWidth: '100%' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <h2 style={{ margin: 0, border: 'none', paddingBottom: 0 }}>Jogos Publicados</h2>
                <button type="button" className="btn btn-primary" style={{ width: 'auto' }} onClick={fetchAllUploads}>
                  {uploadsLoading ? 'Carregando...' : 'Atualizar'}
                </button>
              </div>

              <div className="table-filters">
                <input
                  type="text"
                  className="form-control filter-input"
                  placeholder="Buscar por Game ID..."
                  value={filterText}
                  onChange={e => setFilterText(e.target.value)}
                />
                <select
                  className="form-control filter-input"
                  value={filterStatus}
                  onChange={e => setFilterStatus(e.target.value)}
                >
                  <option value="">Todos os Status</option>
                  {uniqueStatuses.map(s => <option key={s} value={s}>{s}</option>)}
                </select>
                <select
                  className="form-control filter-input"
                  value={filterLoja}
                  onChange={e => setFilterLoja(e.target.value)}
                >
                  <option value="">Todas as Lojas</option>
                  {uniqueLojas.map(l => <option key={l} value={l}>{l}</option>)}
                </select>
              </div>

              {allUploads.length === 0 && !uploadsLoading ? (
                <p style={{ color: '#999', textAlign: 'center', padding: '24px 0' }}>Clique em "Atualizar" para carregar os jogos publicados.</p>
              ) : (
                <div className="table-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Game ID</th>
                        <th>Loja</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredUploads.length === 0 ? (
                        <tr><td colSpan={3} style={{ textAlign: 'center', color: '#999' }}>Nenhum resultado encontrado</td></tr>
                      ) : (
                        filteredUploads.map((u, i) => (
                          <tr key={i}>
                            <td>{u.gameId}</td>
                            <td>{u.loja}</td>
                            <td>
                              <span className={`table-status ${u.status === 'CONCLUIDO' ? 'status-done' : u.status === 'PENDENTE' ? 'status-pending' : ''}`}>
                                {u.status}
                              </span>
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
              )}
              <p style={{ color: '#666', fontSize: '0.8rem', marginTop: '8px' }}>
                {filteredUploads.length} de {allUploads.length} resultado(s)
              </p>
            </section>
          )}

          {activeSection === 'update' && (
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
          )}

          {activeSection === 'delete' && (
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
          )}

          {activeSection === 'settings' && (
            <section className="card settings-card">
              <h2>⚙️ Configurações da Conta</h2>

              {settingsSaved && (
                <div className="settings-toast">✓ Alterações salvas com sucesso!</div>
              )}

              <form onSubmit={handleSettingsSave}>
                <div className="settings-section">
                  <h3 className="settings-section-title">Dados da Empresa</h3>
                  <div className="form-group" style={{ width: '100%' }}>
                    <label>Nome da Empresa</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="Razão Social da Empresa"
                      value={settingsData.nomeEmpresa}
                      onChange={e => setSettingsData({ ...settingsData, nomeEmpresa: e.target.value })}
                    />
                  </div>
                  <div className="form-group" style={{ width: '100%' }}>
                    <label>CNPJ</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="00.000.000/0001-00"
                      value={settingsData.cnpj}
                      onChange={e => setSettingsData({ ...settingsData, cnpj: e.target.value })}
                    />
                  </div>
                </div>

                <div className="settings-section">
                  <h3 className="settings-section-title">Alterar Senha</h3>
                  <div className="form-group" style={{ width: '100%' }}>
                    <label>Senha Atual</label>
                    <input
                      type="password"
                      className="form-control"
                      placeholder="••••••••"
                      value={settingsData.senhaAtual}
                      onChange={e => setSettingsData({ ...settingsData, senhaAtual: e.target.value })}
                    />
                  </div>
                  <div className="form-group" style={{ width: '100%' }}>
                    <label>Nova Senha</label>
                    <input
                      type="password"
                      className="form-control"
                      placeholder="••••••••"
                      value={settingsData.senhaNova}
                      onChange={e => setSettingsData({ ...settingsData, senhaNova: e.target.value })}
                    />
                  </div>
                  <div className="form-group" style={{ width: '100%' }}>
                    <label>Confirmar Nova Senha</label>
                    <input
                      type="password"
                      className="form-control"
                      placeholder="••••••••"
                      value={settingsData.confirmaSenha}
                      onChange={e => setSettingsData({ ...settingsData, confirmaSenha: e.target.value })}
                    />
                  </div>
                </div>

                <button type="submit" className="btn btn-primary" style={{ marginTop: '0.5rem' }}>Salvar Alterações</button>
              </form>
            </section>
          )}

        </div>
      </div>
    </div>
  )
}

export default Dashboard;
