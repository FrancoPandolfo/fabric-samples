package com.code.hyperledger.controllers;

import com.code.hyperledger.models.AssetIdDto;
import com.code.hyperledger.models.Vacuna;
import com.code.hyperledger.models.VacunaDto;
import com.code.hyperledger.services.VacunaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VacunaController.class)
public class VacunaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VacunaService vacunaService;

    @Test
    public void testCrearVacuna() throws Exception {
        doNothing().when(vacunaService).cargarVacuna(any(Vacuna.class));

        mockMvc.perform(post("/vacunas/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientDocumentNumber\":\"12345678\",\"administradedProduct\":\"Vacuna ABC\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.timeStamp").exists());
    }

    @Test
    public void testObtenerVacuna() throws Exception {
        Vacuna vacuna = new Vacuna();
        vacuna.setId("vacuna123");
        vacuna.setPatientDocumentNumber("12345678");
        when(vacunaService.obtenerVacuna(anyString())).thenReturn(vacuna);

        mockMvc.perform(post("/vacunas/obtener")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"vacuna123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("vacuna123"))
                .andExpect(jsonPath("$.patientDocumentNumber").value("12345678"));
    }

    @Test
    public void testObtenerTodasLasVacunas() throws Exception {
        List<Vacuna> vacunas = Arrays.asList(new Vacuna(), new Vacuna());
        when(vacunaService.obtenerTodasLasVacunas()).thenReturn(vacunas);

        mockMvc.perform(get("/vacunas/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(vacunas.size()));
    }

    @Test
    public void testObtenerVacunasPorIds() throws Exception {
        List<Vacuna> vacunas = Collections.singletonList(new Vacuna());
        when(vacunaService.obtenerVacunasPorIds(anyList())).thenReturn(vacunas);

        mockMvc.perform(post("/vacunas/todas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\":[\"id1\",\"id2\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(vacunas.size()));
    }

    @Test
    public void testObtenerVacunasPorDniYEstado() throws Exception {
        List<Vacuna> vacunas = Collections.singletonList(new Vacuna());
        when(vacunaService.obtenerVacunasPorDniYEstado(anyString(), anyString())).thenReturn(vacunas);

        mockMvc.perform(post("/vacunas/filtrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dni\":\"12345678\",\"estado\":\"activo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(vacunas.size()));
    }

    @Test
    public void testCrearVacuna_BadRequestWhenMissingPatientDocumentNumber() throws Exception {
        mockMvc.perform(post("/vacunas/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"administradedProduct\":\"Vacuna ABC\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCrearVacuna_ServiceThrowsException() throws Exception {
        doThrow(new RuntimeException("Error inesperado")).when(vacunaService).cargarVacuna(any(Vacuna.class));

        mockMvc.perform(post("/vacunas/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientDocumentNumber\":\"12345678\",\"administradedProduct\":\"Vacuna ABC\"}"))
                .andExpect(status().isUnauthorized()); // coincide con el controlador que devuelve UNAUTHORIZED (401)
    }

    @Test
    public void testObtenerVacuna_BadRequestWhenMissingId() throws Exception {
        mockMvc.perform(post("/vacunas/obtener")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testObtenerVacuna_ServiceThrowsException() throws Exception {
        when(vacunaService.obtenerVacuna(anyString())).thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(post("/vacunas/obtener")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"vacuna123\"}"))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void testObtenerTodasLasVacunas_ServiceThrowsException() throws Exception {
        when(vacunaService.obtenerTodasLasVacunas()).thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(get("/vacunas/all"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testObtenerVacunasPorIds_ServiceThrowsException() throws Exception {
        when(vacunaService.obtenerVacunasPorIds(anyList())).thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(post("/vacunas/todas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\":[\"id1\"]}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testObtenerVacunasPorDniYEstado_BadRequestWhenMissingDni() throws Exception {
        mockMvc.perform(post("/vacunas/filtrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"activo\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testObtenerVacunasPorDniYEstado_ServiceThrowsException() throws Exception {
        doThrow(new RuntimeException("Error inesperado")).when(vacunaService).obtenerVacunasPorDniYEstado(anyString(), anyString());

        mockMvc.perform(post("/vacunas/filtrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dni\":\"12345678\",\"estado\":\"activo\"}"))
                .andExpect(status().isInternalServerError());
    }
}
