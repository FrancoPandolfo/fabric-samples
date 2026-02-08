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

func TestCreateVacuna(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	vacuna := chaincode.Vacuna{ID: "vacuna1"}

	// Caso normal: vacuna no existe
	stub.GetStateReturns(nil, nil)
	stub.PutStateReturns(nil)
	err := cc.CreateVacuna(ctx, vacuna)
	require.NoError(t, err)

	// Vacuna ya existe
	stub.GetStateReturns([]byte(`some data`), nil)
	err = cc.CreateVacuna(ctx, vacuna)
	require.EqualError(t, err, "la vacuna vacuna1 ya existe")

	// Error en GetState
	stub.GetStateReturns(nil, fmt.Errorf("db error"))
	err = cc.CreateVacuna(ctx, vacuna)
	require.EqualError(t, err, "db error")
}

func TestVacunaExists(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	// Existe
	stub.GetStateReturns([]byte(`data`), nil)
	exists, err := cc.VacunaExists(ctx, "vacuna1")
	require.NoError(t, err)
	require.True(t, exists)

	// No existe
	stub.GetStateReturns(nil, nil)
	exists, err = cc.VacunaExists(ctx, "vacuna1")
	require.NoError(t, err)
	require.False(t, exists)

	// Error
	stub.GetStateReturns(nil, fmt.Errorf("ledger error"))
	_, err = cc.VacunaExists(ctx, "vacuna1")
	require.EqualError(t, err, "ledger error")
}

func TestReadVacuna(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	vacuna := chaincode.Vacuna{ID: "vacuna1"}
	vacunaBytes, _ := json.Marshal(vacuna)

	// Error en GetState
	stub.GetStateReturns(nil, fmt.Errorf("ledger error"))
	_, err := cc.ReadVacuna(ctx, "vacuna1")
	require.EqualError(t, err, "error al leer del ledger: ledger error")

	// Vacuna no existe
	stub.GetStateReturns(nil, nil)
	_, err = cc.ReadVacuna(ctx, "vacuna1")
	require.EqualError(t, err, "la vacuna vacuna1 no existe")

	// Caso normal correcto
	stub.GetStateReturns(vacunaBytes, nil)
	v, err := cc.ReadVacuna(ctx, "vacuna1")
	require.NoError(t, err)
	require.Equal(t, vacuna, *v)
}

func TestGetMultipleVacunas(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	vacuna1 := &chaincode.Vacuna{ID: "vacuna1"}
	vacuna2 := &chaincode.Vacuna{ID: "vacuna2"}
	vacuna1Bytes, _ := json.Marshal(vacuna1)
	vacuna2Bytes, _ := json.Marshal(vacuna2)

	stub.GetStateReturnsOnCall(0, vacuna1Bytes, nil)
	stub.GetStateReturnsOnCall(1, vacuna2Bytes, nil)
	stub.GetStateReturnsOnCall(2, nil, nil) // vacuna inexistente

	ids := []string{"vacuna1", "vacuna2", "vacuna3"}
	vacunas, err := cc.GetMultipleVacunas(ctx, ids)
	require.NoError(t, err)
	require.Len(t, vacunas, 2)
	require.Equal(t, "vacuna1", vacunas[0].ID)
	require.Equal(t, "vacuna2", vacunas[1].ID)

	// Error en GetState
	stub.GetStateReturns(nil, fmt.Errorf("getstate error"))
	_, err = cc.GetMultipleVacunas(ctx, ids)
	require.Error(t, err)
}

func TestGetVacunasPorDniYEstado(t *testing.T) {
	cc := &chaincode.SmartContract{}
	stub := &mocks.ChaincodeStub{}
	ctx := &mocks.TransactionContext{}
	ctx.GetStubReturns(stub)

	vacuna := &chaincode.Vacuna{
		ID:                   "vacuna1",
		PatientDocumentNumber: "1234",
		Status:               "active",
	}
	vacunaBytes, _ := json.Marshal(vacuna)

	iterator := &mocks.StateQueryIterator{}
	iterator.HasNextReturnsOnCall(0, true)
	iterator.HasNextReturnsOnCall(1, false)
	iterator.NextReturns(&queryresult.KV{Value: vacunaBytes}, nil)

	stub.GetStateByRangeReturns(iterator, nil)

	// Caso normal
	vacunas, err := cc.GetVacunasPorDniYEstado(ctx, "1234", "active")
	require.NoError(t, err)
	require.Len(t, vacunas, 1)
	require.Equal(t, "vacuna1", vacunas[0].ID)

	// Error parámetros: dni vacío
	_, err = cc.GetVacunasPorDniYEstado(ctx, "", "active")
	require.Error(t, err)

	// Error al obtener iterator
	stub.GetStateByRangeReturns(nil, fmt.Errorf("range error"))
	_, err = cc.GetVacunasPorDniYEstado(ctx, "1234", "active")
	require.Error(t, err)
}
