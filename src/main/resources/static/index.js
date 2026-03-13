async function criarUpload() {

    const upload = {
        gameId: document.getElementById("gameId").value,
        loja: document.getElementById("loja").value,
        status: document.getElementById("status").value
    };

    await fetch("/uploads", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(upload)
    });

    alert("Upload criado!");
}

async function buscarUpload() {

    const id = document.getElementById("buscarId").value;

    const response = await fetch("/uploads/" + id);

    if (response.status === 404) {
        document.getElementById("resultado").innerText = "Upload não encontrado";
        return;
    }

    const data = await response.json();

    document.getElementById("resultado").innerText =
        JSON.stringify(data, null, 2);
}

async function atualizarStatus() {

    const id = document.getElementById("updateId").value;
    const status = document.getElementById("novoStatus").value;

    await fetch("/uploads/" + id + "?status=" + status, {
        method: "PUT"
    });

    alert("Status atualizado!");
}

async function deletarUpload() {

    const id = document.getElementById("deleteId").value;

    await fetch("/uploads/" + id, {
        method: "DELETE"
    });

    alert("Upload deletado!");
}