package com.projetoDados.HUB.Backend.Mongo.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.projetoDados.HUB.Backend.Mongo.Model.ArquivoDocumento;
import com.projetoDados.HUB.Backend.Mongo.Repository.ArquivoDocumentoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArquivoDocumentoService {

    private final ArquivoDocumentoRepository repository;

    public ArquivoDocumento criar(ArquivoDocumento novo) {
        novo.setId(null);
        return repository.save(novo);
    }

    public List<ArquivoDocumento> listar() {
        return repository.findAll();
    }

    public Optional<ArquivoDocumento> buscarPorId(String id) {
        return repository.findById(id);
    }

    public Optional<ArquivoDocumento> atualizar(String id, ArquivoDocumento atualizacao) {
        return repository.findById(id).map(existente -> salvarAtualizacao(existente, atualizacao));
    }

    public boolean deletar(String id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    private ArquivoDocumento salvarAtualizacao(ArquivoDocumento existente, ArquivoDocumento atualizacao) {
        existente.setArquivo(atualizacao.getArquivo());
        existente.setNome(atualizacao.getNome());
        existente.setDescricao(atualizacao.getDescricao());
        existente.setCategorias(atualizacao.getCategorias());
        existente.setRequisitos(atualizacao.getRequisitos());
        return repository.save(existente);
    }
}

