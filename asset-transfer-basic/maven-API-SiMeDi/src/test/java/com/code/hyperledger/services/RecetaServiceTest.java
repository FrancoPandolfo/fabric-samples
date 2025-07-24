package com.code.hyperledger.services;

import com.code.hyperledger.configs.FabricConfigProperties;
import com.code.hyperledger.models.Receta;
import com.code.hyperledger.services.RecetaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.client.CommitException;
import org.hyperledger.fabric.client.CommitStatusException;
import org.hyperledger.fabric.client.EndorseException;
import org.hyperledger.fabric.client.GatewayException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.hyperledger.fabric.client.SubmitException;
import org.hyperledger.fabric.client.Contract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RecetaServiceTest {

    @Mock
    private FabricConfigProperties config;

    @Mock
    private Contract contract;

    @InjectMocks
    private RecetaService recetaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        recetaService = new RecetaService(config);
        recetaService.setContract(contract);
    }

    @Test
    public void testCargarReceta_Success() throws Exception {
        Receta receta = new Receta();
        receta.setId("id123");
        receta.setMedicacion("Ibuprofeno");

        recetaService.cargarReceta(receta);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(contract, times(1))
            .submitTransaction(eq("CreateReceta"), captor.capture());

        String json = captor.getValue();
        assertTrue(json.contains("id123"));
        assertTrue(json.contains("Ibuprofeno"));
    }

    @Test
    public void testCargarReceta_SubmitThrowsException() throws Exception {
        Receta receta = new Receta();
        StatusRuntimeException cause = Status.ABORTED.asRuntimeException();

        doThrow(new SubmitException("Submit failed", cause))
            .when(contract)
            .submitTransaction(eq("CreateReceta"), anyString());

        // Service catches exception, so no throw
        recetaService.cargarReceta(receta);

        verify(contract, times(1))
            .submitTransaction(eq("CreateReceta"), anyString());
    }

    @Test
    public void testEntregarReceta_SubmitThrowsException_WithStatusUnknown() throws Exception {
        StatusRuntimeException submitCauseUnknown = Status.UNKNOWN.asRuntimeException();
        doThrow(new SubmitException("Submit failed", submitCauseUnknown))
            .when(contract)
            .submitTransaction(eq("EntregarReceta"), eq("id"));

        assertThrows(SubmitException.class, () -> recetaService.entregarReceta("id"));

        verify(contract).submitTransaction("EntregarReceta", "id");
    }

    @Test
    public void testFirmarReceta_SubmitThrowsCommitException_ValidStatus() throws Exception {
        CommitException commitExceptionMock = mock(CommitException.class);

        doThrow(commitExceptionMock)
            .when(contract)
            .submitTransaction(eq("FirmarReceta"), eq("id"), eq("sign"));

        assertThrows(CommitException.class, () -> recetaService.firmarReceta("id", "sign"));

        verify(contract).submitTransaction("FirmarReceta", "id", "sign");
    }

    @Test
    public void testObtenerReceta_Success() throws Exception {
        Receta recetaMock = new Receta();
        recetaMock.setId("receta123");
        recetaMock.setMedicacion("Paracetamol");

        String recetaJson = objectMapper.writeValueAsString(recetaMock);
        when(contract.evaluateTransaction("ReadReceta", "receta123"))
            .thenReturn(recetaJson.getBytes());

        Receta result = recetaService.obtenerReceta("receta123");

        assertNotNull(result);
        assertEquals("receta123", result.getId());
        assertEquals("Paracetamol", result.getMedicacion());

        verify(contract).evaluateTransaction("ReadReceta", "receta123");
    }

    @Test
    public void testObtenerReceta_EvaluateThrowsException() throws Exception {
        StatusRuntimeException grpcCause = Status.INTERNAL.withDescription("Evaluate failed").asRuntimeException();

        when(contract.evaluateTransaction("ReadReceta", "id"))
            .thenThrow(new GatewayException(grpcCause));

        assertThrows(GatewayException.class, () -> recetaService.obtenerReceta("id"));

        verify(contract).evaluateTransaction("ReadReceta", "id");
    }

    @Test
    public void testObtenerTodasLasRecetas_Success() throws Exception {
        List<Receta> recetas = Arrays.asList(new Receta(), new Receta());
        String json = objectMapper.writeValueAsString(recetas);
        when(contract.evaluateTransaction("GetAllRecetas"))
            .thenReturn(json.getBytes());

        List<Receta> result = recetaService.obtenerTodasLasRecetas();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(contract).evaluateTransaction("GetAllRecetas");
    }

    @Test
    public void testObtenerTodasLasRecetas_EvaluateThrowsException() throws Exception {
        StatusRuntimeException grpcCause = Status.INTERNAL.withDescription("Fail").asRuntimeException();

        when(contract.evaluateTransaction("GetAllRecetas"))
            .thenThrow(new GatewayException(grpcCause));

        assertThrows(GatewayException.class, () -> recetaService.obtenerTodasLasRecetas());

        verify(contract).evaluateTransaction("GetAllRecetas");
    }

    @Test
    public void testObtenerRecetasPorIds_Success() throws Exception {
        List<String> ids = Arrays.asList("id1", "id2");
        List<Receta> recetas = Arrays.asList(new Receta(), new Receta());

        String jsonInput = objectMapper.writeValueAsString(ids);
        String jsonOutput = objectMapper.writeValueAsString(recetas);

        when(contract.evaluateTransaction("GetMultipleRecetas", jsonInput))
            .thenReturn(jsonOutput.getBytes());

        List<Receta> result = recetaService.obtenerRecetasPorIds(ids);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(contract).evaluateTransaction("GetMultipleRecetas", jsonInput);
    }

    @Test
    public void testObtenerRecetasPorIds_EvaluateThrowsException() throws Exception {
        List<String> ids = Arrays.asList("id1");
        String jsonInput = objectMapper.writeValueAsString(ids);

        StatusRuntimeException grpcCause = Status.INTERNAL.withDescription("Fail").asRuntimeException();

        when(contract.evaluateTransaction("GetMultipleRecetas", jsonInput))
            .thenThrow(new GatewayException(grpcCause));

        assertThrows(GatewayException.class, () -> recetaService.obtenerRecetasPorIds(ids));

        verify(contract).evaluateTransaction("GetMultipleRecetas", jsonInput);
    }

    @Test
    public void testEntregarReceta_Success() throws Exception {
        recetaService.entregarReceta("id123");

        verify(contract).submitTransaction("EntregarReceta", "id123");
    }

    @Test
    public void testEntregarReceta_SubmitThrowsException() throws Exception {
        doThrow(new SubmitException("Submit failed", Status.UNKNOWN.asRuntimeException()))
            .when(contract)
            .submitTransaction(eq("EntregarReceta"), eq("id"));

        assertThrows(SubmitException.class, () -> recetaService.entregarReceta("id"));

        verify(contract).submitTransaction("EntregarReceta", "id");
    }

    @Test
    public void testFirmarReceta_Success() throws Exception {
        recetaService.firmarReceta("id123", "sign");

        verify(contract).submitTransaction("FirmarReceta", "id123", "sign");
    }

    @Test
    public void testFirmarReceta_SubmitThrowsCommitException() throws Exception {
        CommitException commitExceptionMock = mock(CommitException.class);

        doThrow(commitExceptionMock)
            .when(contract)
            .submitTransaction(eq("FirmarReceta"), eq("id"), eq("sign"));

        assertThrows(CommitException.class, () -> recetaService.firmarReceta("id", "sign"));

        verify(contract).submitTransaction("FirmarReceta", "id", "sign");
    }

    @Test
    public void testBorrarReceta_Success() throws Exception {
        recetaService.borrarReceta("id123");

        verify(contract).submitTransaction("DeleteReceta", "id123");
    }

    @Test
    public void testBorrarReceta_SubmitThrowsException() throws Exception {
        StatusRuntimeException endorseCause = Status.PERMISSION_DENIED.asRuntimeException();
        doThrow(new EndorseException("Delete failed", endorseCause))
            .when(contract)
            .submitTransaction(eq("DeleteReceta"), eq("id"));

        assertThrows(EndorseException.class, () -> recetaService.borrarReceta("id"));

        verify(contract).submitTransaction("DeleteReceta", "id");
    }
}

    // initLedger() and init() are covered by integration tests since they connect to a real Fabric network
// Removed stray closing brace
