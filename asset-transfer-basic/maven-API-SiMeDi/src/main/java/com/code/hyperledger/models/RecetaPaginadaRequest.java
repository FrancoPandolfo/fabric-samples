package main.java.com.code.hyperledger.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecetaPaginadaRequest {
    private String dni;
    private String estado;
    private int pageSize;
    private String bookmark; 
}
