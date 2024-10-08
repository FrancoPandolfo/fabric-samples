package com.code.hyperledger.coso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecetaDto {
    private String owner;
    private String prescripcionAnteriorId;
    private String status;
    //@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String statusChange;
    private String prioridad;
    private String medicacion;
    private String razon;
    private String notas;
    private String periodoDeTratamiento;
    private String instruccionesTratamiento;
    private String periodoDeValidez;
    private String dniPaciente;
    //@JsonFormat(pattern = "yyyy-MM-dd")
    private String fechaDeAutorizacion;
    private int cantidad;
    //@JsonFormat(pattern = "yyyy-MM-dd")
    private String expectedSupplyDuration;
}
