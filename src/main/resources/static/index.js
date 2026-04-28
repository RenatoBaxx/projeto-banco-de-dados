async function enterGame() {
  const gameId = document.getElementById("enterGameId").value;
  const userId = document.getElementById("enterUserId").value;

  await fetch(`/stats/${gameId}/enter?userId=${userId}`, {
    method: "POST",
  });

  alert("Usuário entrou no jogo!");
}

async function leaveGame() {
  const gameId = document.getElementById("leaveGameId").value;
  const userId = document.getElementById("leaveUserId").value;

  await fetch(`/stats/${gameId}/leave?userId=${userId}`, {
    method: "POST",
  });

  alert("Usuário saiu do jogo!");
}

async function getStats() {
  const gameId = document.getElementById("statsGameId").value;

  const response = await fetch(`/stats/${gameId}`);

  if (response.status === 404) {
    document.getElementById("statsResult").innerText = "Não encontrado";
    return;
  }

  const data = await response.json();

  document.getElementById("statsResult").innerText = JSON.stringify(
    data,
    null,
    2,
  );
}

async function getUsers() {
  const gameId = document.getElementById("usersGameId").value;

  const response = await fetch(`/stats/${gameId}/users`);
  const data = await response.json();

  document.getElementById("usersResult").innerText = JSON.stringify(
    data,
    null,
    2,
  );
}

async function getRanking() {
  const response = await fetch(`/stats/ranking`);
  const data = await response.json();

  document.getElementById("rankingResult").innerText = JSON.stringify(
    data,
    null,
    2,
  );
}
