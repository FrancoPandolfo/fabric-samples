package com.code.hyperledger.controllers;

import com.code.hyperledger.models.Receta;
import com.code.hyperledger.services.RecetaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecetaController.class)
public class RecetaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecetaService recetaService;

    @Test
    public void testCrearReceta() throws Exception {
        doNothing().when(recetaService).cargarReceta(any(Receta.class));

        mockMvc.perform(post("/recetas/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientDocumentNumber\":\"12345678\",\"medicacion\":\"Paracetamol\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testObtenerReceta() throws Exception {
        Receta receta = new Receta();
        receta.setId("123");
        when(recetaService.obtenerReceta(anyString())).thenReturn(receta);

        mockMvc.perform(post("/recetas/obtener")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testObtenerRecetasPorIds() throws Exception {
        List<Receta> recetas = Collections.singletonList(new Receta());
        when(recetaService.obtenerRecetasPorIds(anyList())).thenReturn(recetas);

        mockMvc.perform(post("/recetas/todas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\":[\"id1\",\"id2\"]}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testEntregarReceta() throws Exception {
        doNothing().when(recetaService).entregarReceta(anyString());

        mockMvc.perform(put("/recetas/entregar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testFirmarReceta() throws Exception {
        doNothing().when(recetaService).firmarReceta(anyString(), anyString());

        mockMvc.perform(put("/recetas/firmar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"123\", \"signature\":\"firma123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testBorrarReceta() throws Exception {
        doNothing().when(recetaService).borrarReceta(anyString());

        mockMvc.perform(post("/recetas/borrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"123\"}"))
                .andExpect(status().isOk());
    }
}
