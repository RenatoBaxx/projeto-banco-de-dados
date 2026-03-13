package com.projetoDados.HUB.Backend.Redis.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Upload {

    private String gameId;
    private String loja;
    private String status;

}