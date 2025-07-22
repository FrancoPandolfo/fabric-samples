package chaincode_test

import (
	"encoding/json"
	"fmt"
	"testing"

	"github.com/hyperledger/fabric-protos-go-apiv2/ledger/queryresult"
	"github.com/stretchr/testify/require"

	"github.com/hyperledger/fabric-samples/asset-transfer-basic/chaincode-go/chaincode"
	"github.com/hyperledger/fabric-samples/asset-transfer-basic/chaincode-go/chaincode/mocks"
)

func TestInitLedger(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	// Normal, todo bien
	stub.PutStateReturns(nil)
	err := cc.InitLedger(ctx)
	require.NoError(t, err)

	// Error en PutState
	stub.PutStateReturns(fmt.Errorf("failed to put to world state"))
	err = cc.InitLedger(ctx)
	require.Error(t, err)
	require.Contains(t, err.Error(), "failed to put to world state")
}

func TestCreateReceta(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	receta := chaincode.Receta{ID: "receta1"}

	// Caso normal: receta no existe
	stub.GetStateReturns(nil, nil)
	stub.PutStateReturns(nil)
	err := cc.CreateReceta(ctx, receta)
	require.NoError(t, err)

	// Receta ya existe
	stub.GetStateReturns([]byte(`some data`), nil)
	err = cc.CreateReceta(ctx, receta)
	require.EqualError(t, err, "la receta receta1 ya existe")

	// Error en GetState
	stub.GetStateReturns(nil, fmt.Errorf("db error"))
	err = cc.CreateReceta(ctx, receta)
	require.EqualError(t, err, "error al acceder al ledger: db error")

	// Error en Marshal (imposible en este caso porque Receta es simple, no testeamos)
}

func TestFirmarReceta(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	receta := chaincode.Receta{
		ID:     "receta1",
		Status: string(chaincode.EstadoDraft),
	}
	recetaBytes, _ := json.Marshal(receta)

	// Receta no existe
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)

	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)

	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)
	stub.GetStateReturns(nil, nil)

	stub.GetStateReturns(nil, nil)

	// Prueba receta no existe
	stub.GetStateReturns(nil, nil)
	err := cc.FirmarReceta(ctx, "receta1", "firma123")
	require.EqualError(t, err, "la receta receta1 no existe")

	// Error en GetState
	stub.GetStateReturns(nil, fmt.Errorf("ledger error"))
	err = cc.FirmarReceta(ctx, "receta1", "firma123")
	require.EqualError(t, err, "error al acceder al ledger: ledger error")

	// Receta encontrada pero estado != draft
	notDraftReceta := receta
	notDraftReceta.Status = string(chaincode.EstadoActive)
	notDraftBytes, _ := json.Marshal(notDraftReceta)
	stub.GetStateReturns(notDraftBytes, nil)
	err = cc.FirmarReceta(ctx, "receta1", "firma123")
	require.EqualError(t, err, "la receta receta1 no puede ser firmada porque no está en estado 'draft'")

	// Caso normal correcto
	stub.GetStateReturns(recetaBytes, nil)
	stub.PutStateReturns(nil)
	err = cc.FirmarReceta(ctx, "receta1", "firma123")
	require.NoError(t, err)
}

func TestEntregarReceta(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	receta := chaincode.Receta{
		ID:     "receta1",
		Status: string(chaincode.EstadoActive),
	}
	recetaBytes, _ := json.Marshal(receta)

	// Receta no existe
	stub.GetStateReturns(nil, nil)
	err := cc.EntregarReceta(ctx, "receta1")
	require.EqualError(t, err, "la receta receta1 no existe")

	// Error al obtener receta
	stub.GetStateReturns(nil, fmt.Errorf("ledger error"))
	err = cc.EntregarReceta(ctx, "receta1")
	require.EqualError(t, err, "error al acceder al ledger: ledger error")

	// Receta encontrada pero estado != active
	notActiveReceta := receta
	notActiveReceta.Status = string(chaincode.EstadoDraft)
	notActiveBytes, _ := json.Marshal(notActiveReceta)
	stub.GetStateReturns(notActiveBytes, nil)
	err = cc.EntregarReceta(ctx, "receta1")
	require.EqualError(t, err, "solo se puede entregar la receta si está en estado 'active'")

	// Caso normal correcto
	stub.GetStateReturns(recetaBytes, nil)
	stub.PutStateReturns(nil)
	err = cc.EntregarReceta(ctx, "receta1")
	require.NoError(t, err)
}

func TestReadReceta(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	receta := chaincode.Receta{
		ID: "receta1",
	}
	recetaBytes, _ := json.Marshal(receta)

	// Error en GetState
	stub.GetStateReturns(nil, fmt.Errorf("ledger error"))
	_, err := cc.ReadReceta(ctx, "receta1")
	require.EqualError(t, err, "error al leer del ledger: ledger error")

	// Receta no existe
	stub.GetStateReturns(nil, nil)
	_, err = cc.ReadReceta(ctx, "receta1")
	require.EqualError(t, err, "la receta receta1 no existe")

	// Caso normal correcto
	stub.GetStateReturns(recetaBytes, nil)
	rec, err := cc.ReadReceta(ctx, "receta1")
	require.NoError(t, err)
	require.Equal(t, receta, *rec)
}

func TestDeleteReceta(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	// Receta existe, estado draft para poder eliminar
	receta := chaincode.Receta{
		ID:     "receta1",
		Status: string(chaincode.EstadoDraft),
	}
	recetaBytes, _ := json.Marshal(receta)

	// Caso normal correcto
	stub.GetStateReturns(recetaBytes, nil)
	stub.PutStateReturns(nil)
	err := cc.DeleteReceta(ctx, "receta1")
	require.NoError(t, err)

	// Receta no existe
	stub.GetStateReturns(nil, nil)
	err = cc.DeleteReceta(ctx, "receta1")
	require.EqualError(t, err, "la receta receta1 no existe")

	// Error al obtener receta
	stub.GetStateReturns(nil, fmt.Errorf("ledger error"))
	err = cc.DeleteReceta(ctx, "receta1")
	require.EqualError(t, err, "error al acceder al ledger: ledger error")

	// Receta estado distinto a draft
	notDraftReceta := receta
	notDraftReceta.Status = string(chaincode.EstadoActive)
	notDraftBytes, _ := json.Marshal(notDraftReceta)
	stub.GetStateReturns(notDraftBytes, nil)
	err = cc.DeleteReceta(ctx, "receta1")
	require.EqualError(t, err, "la receta receta1 no puede ser firmada porque no está en estado 'draft'")
}

func TestRecetaExists(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	// Existe
	stub.GetStateReturns([]byte(`data`), nil)
	exists, err := cc.RecetaExists(ctx, "receta1")
	require.NoError(t, err)
	require.True(t, exists)

	// No existe
	stub.GetStateReturns(nil, nil)
	exists, err = cc.RecetaExists(ctx, "receta1")
	require.NoError(t, err)
	require.False(t, exists)

	// Error
	stub.GetStateReturns(nil, fmt.Errorf("ledger error"))
	_, err = cc.RecetaExists(ctx, "receta1")
	require.EqualError(t, err, "error al acceder al ledger: ledger error")
}

func TestTransferirReceta(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	receta := chaincode.Receta{ID: "receta1", Owner: "owner1"}
	recetaBytes, _ := json.Marshal(receta)

	// Caso normal
	stub.GetStateReturns(recetaBytes, nil)
	stub.PutStateReturns(nil)
	oldOwner, err := cc.TransferirReceta(ctx, "receta1", "newOwner")
	require.NoError(t, err)
	require.Equal(t, "owner1", oldOwner)

	// Error en GetState
	stub.GetStateReturns(nil, fmt.Errorf("ledger error"))
	_, err = cc.TransferirReceta(ctx, "receta1", "newOwner")
	require.EqualError(t, err, "error al leer del ledger: ledger error")
}

func TestGetAllRecetas(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	receta := &chaincode.Receta{ID: "receta1", Status: string(chaincode.EstadoActive)}
	recetaBytes, _ := json.Marshal(receta)

	iterator := &mocks.StateQueryIterator{}
	iterator.HasNextReturnsOnCall(0, true)
	iterator.HasNextReturnsOnCall(1, false)
	iterator.NextReturns(&queryresult.KV{Value: recetaBytes}, nil)

	stub.GetStateByRangeReturns(iterator, nil)
	recetas, err := cc.GetAllRecetas(ctx)
	require.NoError(t, err)
	require.Len(t, recetas, 1)
	require.Equal(t, receta.ID, recetas[0].ID)

	// Error al iterar
	iterator.HasNextReturns(true)
	iterator.NextReturns(nil, fmt.Errorf("iter error"))
	recetas, err = cc.GetAllRecetas(ctx)
	require.EqualError(t, err, "iter error")
	require.Nil(t, recetas)

	// Error al obtener iterator
	stub.GetStateByRangeReturns(nil, fmt.Errorf("range error"))
	recetas, err = cc.GetAllRecetas(ctx)
	require.EqualError(t, err, "range error")
	require.Nil(t, recetas)
}

func TestGetMultipleRecetas(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	receta1 := &chaincode.Receta{ID: "receta1", Status: string(chaincode.EstadoActive)}
	receta2 := &chaincode.Receta{ID: "receta2", Status: string(chaincode.EstadoCancelled)}
	receta1Bytes, _ := json.Marshal(receta1)
	receta2Bytes, _ := json.Marshal(receta2)

	stub.GetStateReturnsOnCall(0, receta1Bytes, nil)
	stub.GetStateReturnsOnCall(1, receta2Bytes, nil)
	stub.GetStateReturnsOnCall(2, nil, nil) // receta inexistente

	ids := []string{"receta1", "receta2", "receta3"}
	recetas, err := cc.GetMultipleRecetas(ctx, ids)
	require.NoError(t, err)
	require.Len(t, recetas, 1) // sólo la receta1 porque la 2 está cancelada y la 3 no existe
	require.Equal(t, "receta1", recetas[0].ID)

	// Error en GetState
	stub.GetStateReturns(nil, fmt.Errorf("getstate error"))
	_, err = cc.GetMultipleRecetas(ctx, ids)
	require.Error(t, err)
}

func TestGetRecetasPorDniYEstado(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	receta := &chaincode.Receta{
		ID:                    "receta1",
		PatientDocumentNumber:  "1234",
		Status:                string(chaincode.EstadoActive),
	}
	recetaBytes, _ := json.Marshal(receta)

	iterator := &mocks.StateQueryIterator{}
	iterator.HasNextReturnsOnCall(0, true)
	iterator.HasNextReturnsOnCall(1, false)
	iterator.NextReturns(&queryresult.KV{Value: recetaBytes}, nil)

	stub.GetStateByRangeReturns(iterator, nil)

	// Caso normal
	recetas, err := cc.GetRecetasPorDniYEstado(ctx, "1234", string(chaincode.EstadoActive))
	require.NoError(t, err)
	require.Len(t, recetas, 1)
	require.Equal(t, "receta1", recetas[0].ID)

	// Error parametros
	_, err = cc.GetRecetasPorDniYEstado(ctx, "", "")
	require.Error(t, err)

	// Error en GetStateByRange
	stub.GetStateByRangeReturns(nil, fmt.Errorf("range error"))
	_, err = cc.GetRecetasPorDniYEstado(ctx, "1234", string(chaincode.EstadoActive))
	require.Error(t, err)
}
