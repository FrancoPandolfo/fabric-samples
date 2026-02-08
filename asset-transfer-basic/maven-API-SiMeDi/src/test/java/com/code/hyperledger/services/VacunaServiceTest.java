package com.code.hyperledger.services;

import com.code.hyperledger.configs.FabricConfigProperties;
import com.code.hyperledger.models.Vacuna;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.client.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class VacunaServiceTest {

    @Mock
    private FabricConfigProperties config;

    @Mock
    private Contract contract;

    @InjectMocks
    private VacunaService vacunaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        vacunaService = new VacunaService(config);
        Field contractField = VacunaService.class.getDeclaredField("contract");
        contractField.setAccessible(true);
        contractField.set(vacunaService, contract);
    }

    @Test
    public void testCargarVacuna_Success() throws Exception {
        Vacuna vacuna = new Vacuna();
        vacuna.setId("id123");
        vacuna.setPatientDocumentNumber("12345678");

        vacunaService.cargarVacuna(vacuna);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(contract, times(1))
            .submitTransaction(eq("CreateVacuna"), captor.capture());

        String json = captor.getValue();
        assertTrue(json.contains("id123"));
        assertTrue(json.contains("12345678"));
    }

    @Test
    public void testCargarVacuna_SubmitThrowsException() throws Exception {
        Vacuna vacuna = new Vacuna();
        StatusRuntimeException cause = Status.ABORTED.asRuntimeException();

        doThrow(new SubmitException("Submit failed", cause))
            .when(contract)
            .submitTransaction(eq("CreateVacuna"), anyString());

        vacunaService.cargarVacuna(vacuna);

        verify(contract, times(1))
            .submitTransaction(eq("CreateVacuna"), anyString());
    }

    @Test
    public void testObtenerVacuna_Success() throws Exception {
        Vacuna vacunaMock = new Vacuna();
        vacunaMock.setId("vacuna123");
        vacunaMock.setPatientDocumentNumber("12345678");

        String vacunaJson = objectMapper.writeValueAsString(vacunaMock);
        when(contract.evaluateTransaction("ReadVacuna", "vacuna123"))
            .thenReturn(vacunaJson.getBytes());

        Vacuna result = vacunaService.obtenerVacuna("vacuna123");

        assertNotNull(result);
        assertEquals("vacuna123", result.getId());
        assertEquals("12345678", result.getPatientDocumentNumber());

        verify(contract).evaluateTransaction("ReadVacuna", "vacuna123");
    }

    @Test
    public void testObtenerVacuna_EvaluateThrowsException() throws Exception {
        StatusRuntimeException grpcCause = Status.INTERNAL.withDescription("Evaluate failed").asRuntimeException();

        when(contract.evaluateTransaction("ReadVacuna", "id"))
            .thenThrow(new GatewayException(grpcCause));

        assertThrows(GatewayException.class, () -> vacunaService.obtenerVacuna("id"));

        verify(contract).evaluateTransaction("ReadVacuna", "id");
    }

    @Test
    public void testObtenerTodasLasVacunas_Success() throws Exception {
        List<Vacuna> vacunas = Arrays.asList(new Vacuna(), new Vacuna());
        String json = objectMapper.writeValueAsString(vacunas);
        when(contract.evaluateTransaction("GetAllVacunas"))
            .thenReturn(json.getBytes());

        List<Vacuna> result = vacunaService.obtenerTodasLasVacunas();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(contract).evaluateTransaction("GetAllVacunas");
    }

    @Test
    public void testObtenerTodasLasVacunas_EvaluateThrowsException() throws Exception {
        StatusRuntimeException grpcCause = Status.INTERNAL.withDescription("Fail").asRuntimeException();

        when(contract.evaluateTransaction("GetAllVacunas"))
            .thenThrow(new GatewayException(grpcCause));

        assertThrows(GatewayException.class, () -> vacunaService.obtenerTodasLasVacunas());

        verify(contract).evaluateTransaction("GetAllVacunas");
    }

    @Test
    public void testObtenerVacunasPorIds_Success() throws Exception {
        List<String> ids = Arrays.asList("id1", "id2");
        List<Vacuna> vacunas = Arrays.asList(new Vacuna(), new Vacuna());

        String jsonInput = objectMapper.writeValueAsString(ids);
        String jsonOutput = objectMapper.writeValueAsString(vacunas);

        when(contract.evaluateTransaction("GetMultipleVacunas", jsonInput))
            .thenReturn(jsonOutput.getBytes());

        List<Vacuna> result = vacunaService.obtenerVacunasPorIds(ids);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(contract).evaluateTransaction("GetMultipleVacunas", jsonInput);
    }

    @Test
    public void testObtenerVacunasPorIds_EvaluateThrowsException() throws Exception {
        List<String> ids = Arrays.asList("id1");
        String jsonInput = objectMapper.writeValueAsString(ids);

        StatusRuntimeException grpcCause = Status.INTERNAL.withDescription("Fail").asRuntimeException();

        when(contract.evaluateTransaction("GetMultipleVacunas", jsonInput))
            .thenThrow(new GatewayException(grpcCause));

        assertThrows(GatewayException.class, () -> vacunaService.obtenerVacunasPorIds(ids));

        verify(contract).evaluateTransaction("GetMultipleVacunas", jsonInput);
    }
}
