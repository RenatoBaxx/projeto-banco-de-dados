package com.projetoDados.HUB.Backend.Redis.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStats {

    private String gameId;
    private Long online;
    private Long max;
    private Long min;
}