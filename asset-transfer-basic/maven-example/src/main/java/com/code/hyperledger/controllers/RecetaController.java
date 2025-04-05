package com.code.hyperledger.controllers;

import com.code.hyperledger.coso.Receta;
import com.code.hyperledger.coso.RecetaDto;
import com.code.hyperledger.coso.AssetIdDto;
import com.code.hyperledger.Utils.Hashing;
import com.code.hyperledger.services.RecetaService;
import org.hyperledger.fabric.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recetas")
public class RecetaController {

    @Autowired
    private RecetaService recetaService;

    @PostMapping("/crear")
    public ResponseEntity<AssetIdDto> crear(@RequestBody Receta receta) {
        System.out.println("\n--> Submit Transaction: CrearReceta");

        String now = LocalDateTime.now().toString();
        String dni = receta.getDniPaciente();
        String id = dni + now;
        String assetId = Hashing.sha256(id);
        receta.setId(assetId);

        AssetIdDto assetIdDto = new AssetIdDto();
        assetIdDto.setDni(dni);
        assetIdDto.setTimeStamp(now);

        try {
            recetaService.cargarReceta(receta);
            return new ResponseEntity<>(assetIdDto, HttpStatus.OK);
        } catch (CommitStatusException | EndorseException | CommitException | SubmitException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/obtener")
    public ResponseEntity<RecetaDto> find(@RequestBody Map<String, String> requestBody) {
        try {
            String id = requestBody.get("id");
            Receta receta = recetaService.obtenerReceta(id);

            RecetaDto recetaDto = mapToDto(receta);
            return new ResponseEntity<>(recetaDto, HttpStatus.OK);
        } catch (IOException | GatewayException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping("/todas")
    public ResponseEntity<List<RecetaDto>> obtenerRecetasPorIds(@RequestBody Map<String, List<String>> requestBody) {
        try {
            List<String> ids = requestBody.get("ids");
            if (ids == null || ids.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            List<Receta> recetas = recetaService.obtenerRecetasPorIds(ids);
            List<RecetaDto> recetasDto = new ArrayList<>();

            for (Receta receta : recetas) {
                recetasDto.add(mapToDto(receta));
            }

            return new ResponseEntity<>(recetasDto, HttpStatus.OK);
        } catch (IOException | GatewayException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private RecetaDto mapToDto(Receta receta) {
        RecetaDto dto = new RecetaDto();
        dto.setOwner(receta.getOwner());
        dto.setPrescripcionAnteriorId(receta.getPrescripcionAnteriorId());
        dto.setStatus(receta.getStatus());
        dto.setStatusChange(receta.getStatusChange());
        dto.setPrioridad(receta.getPrioridad());
        dto.setMedicacion(receta.getMedicacion());
        dto.setRazon(receta.getRazon());
        dto.setNotas(receta.getNotas());
        dto.setPeriodoDeTratamiento(receta.getPeriodoDeTratamiento());
        dto.setInstruccionesTratamiento(receta.getInstruccionesTratamiento());
        dto.setPeriodoDeValidez(receta.getPeriodoDeValidez());
        dto.setDniPaciente(receta.getDniPaciente());
        dto.setFechaDeAutorizacion(receta.getFechaDeAutorizacion());
        dto.setCantidad(receta.getCantidad());
        dto.setExpectedSupplyDuration(receta.getExpectedSupplyDuration());
        return dto;
    }
}
