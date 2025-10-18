package com.code.hyperledger.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultadoPaginado<T> {
    private List<T> componentes;
    private String bookmark;
}
