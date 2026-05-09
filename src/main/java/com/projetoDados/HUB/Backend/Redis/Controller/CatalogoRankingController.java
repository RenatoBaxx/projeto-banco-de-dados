package com.projetoDados.HUB.Backend.Redis.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projetoDados.HUB.Backend.Redis.DTO.RankingJogoItemResponse;
import com.projetoDados.HUB.Backend.Redis.Service.CatalogoJogoRankingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogoRankingController {

    private final CatalogoJogoRankingService catalogoJogoRankingService;

    @GetMapping("/ranking")
    public ResponseEntity<List<RankingJogoItemResponse>> getRanking() {
        return ResponseEntity.ok(catalogoJogoRankingService.listarRanking());
    }
}
