package com.code.hyperledger.controllers;

import com.code.hyperledger.Utils.Hashing;
import com.code.hyperledger.models.AssetIdDto;
import com.code.hyperledger.models.Receta;
import com.code.hyperledger.models.RecetaDto;
import com.code.hyperledger.models.Vacuna;
import com.code.hyperledger.models.VacunaDto;
import com.code.hyperledger.models.Vacuna;
import com.code.hyperledger.services.VacunaService;

import com.code.hyperledger.models.ResultadoPaginado;

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
@RequestMapping("/vacunas")
public class VacunaController {

    @Autowired
    private VacunaService vacunaService;

    @PostMapping("/crear")
    public ResponseEntity<AssetIdDto> crearVacuna(@RequestBody Vacuna vacuna) {

        if (vacuna == null || vacuna.getPatientDocumentNumber() == null || vacuna.getPatientDocumentNumber().isEmpty()) {
            System.err.println("Datos de vacuna inválidos: faltan campos requeridos.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String now = LocalDateTime.now().toString();
        String dni = vacuna.getPatientDocumentNumber();
    
        String id = dni + now;
        String assetId = Hashing.sha256(id);
    

        System.out.println("ID generado para el asset: " + assetId);

        vacuna.setId(assetId);

        AssetIdDto assetIdDto = new AssetIdDto();
        assetIdDto.setDni(dni);
        assetIdDto.setTimeStamp(now);

        try {
    
            vacunaService.cargarVacuna(vacuna);
            return new ResponseEntity<>(assetIdDto, HttpStatus.OK);

        } catch (CommitStatusException | EndorseException | CommitException | SubmitException e) {
            
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (RuntimeException e) {
            System.err.println("Error inesperado al registrar la vacuna: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }    

    @PostMapping("/obtener")
    public ResponseEntity<Vacuna> obtenerVacuna(@RequestBody Map<String, String> requestBody) {
        try {
            String id = requestBody.get("id");
            if (id == null || id.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Vacuna vacuna = vacunaService.obtenerVacuna(id);
            return new ResponseEntity<>(vacuna, HttpStatus.OK);

        } catch (IOException | GatewayException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);

        } catch (RuntimeException e) {
            System.err.println("Error inesperado al obtener la vacuna: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Vacuna>> obtenerTodasLasVacunas() {
        try {
            List<Vacuna> vacunas = vacunaService.obtenerTodasLasVacunas();
            return new ResponseEntity<>(vacunas, HttpStatus.OK);
        } catch (IOException | GatewayException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/todas")
    public ResponseEntity<List<VacunaDto>> obtenerVacunasPorIds(@RequestBody Map<String, List<String>> requestBody) {
        try {
            List<String> ids = requestBody.get("ids");
            if (ids == null || ids.isEmpty()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
            }

            List<Vacuna> vacunas = vacunaService.obtenerVacunasPorIds(ids);
            List<VacunaDto> vacunasDto = new ArrayList<>();

            for (Vacuna vacuna : vacunas) {
                vacunasDto.add(mapToDto(vacuna));
            }

            return new ResponseEntity<>(vacunasDto, HttpStatus.OK);
        } catch (IOException | GatewayException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/obtener/paginado")
    public ResponseEntity<ResultadoPaginado<Vacuna>> obtenerVacunasPorDniPaginado(
            @RequestParam String dni,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String bookmark) {
        try {
            if (dni == null || dni.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            ResultadoPaginado<Vacuna> vacunas = vacunaService.obtenerVacunasPorDniPaginado(dni, pageSize, bookmark);
            return new ResponseEntity<>(vacunas, HttpStatus.OK);
        } catch (IOException | GatewayException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    private VacunaDto mapToDto(Vacuna vacuna) {
        VacunaDto dto = new VacunaDto();
        dto.setIdentificador(vacuna.getIdentifier());
        dto.setStatus(vacuna.getStatus());
        dto.setStatusChange(vacuna.getStatusChange());
        dto.setStatusReason(vacuna.getStatusReason());
        dto.setVaccinateCode(vacuna.getVaccinateCode());
        dto.setAdministradedProduct(vacuna.getAdministradedProduct());
        dto.setManufacturer(vacuna.getManufacturer());
        dto.setLotNumber(vacuna.getLotNumber());
        dto.setExpirationDate(vacuna.getExpirationDate());
        dto.setPatientDocumentNumber(vacuna.getPatientDocumentNumber());
        dto.setReactions(vacuna.getReactions());
        dto.setMatricula(vacuna.getMatricula());
        dto.setPractitioner(vacuna.getPractitioner());
        dto.setPractitionerDocumentNumber(vacuna.getPractitionerDocumentNumber());

        return dto;
    }
    
}
