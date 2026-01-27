#!/bin/bash
set -e

# Versión base (si no se pasa nada, usa 1.0)
VERSION_LABEL=${1:-"1.0"}

echo "--- Iniciando Despliegue Inteligente ---"

# 1. Entrar al directorio donde vive el script de red y la carpeta organizations
cd test-network

# 2. Configurar variables de entorno para usar el binario 'peer'
# Asumimos que la carpeta 'bin' y 'config' de fabric-samples están 3 niveles arriba
export PATH=${PWD}/../../../bin:$PATH
export FABRIC_CFG_PATH=${PWD}/../../../config/

# --- CONFIGURACIÓN DE RUTAS DE CRIPTO ---
# Usamos ${PWD} que ahora es ".../test-network", donde confirmaste que está 'organizations'
CRYPTO_BASE="${PWD}/organizations"

export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=${CRYPTO_BASE}/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=${CRYPTO_BASE}/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=localhost:7051

echo "--- Consultando secuencia a la red ---"
# Verificamos si podemos hablar con el peer
if ! peer lifecycle chaincode querycommitted --channelID mychannel --name basic > /dev/null 2>&1; then
    echo "No se pudo contactar al Peer o el chaincode 'basic' no existe aún."
    CURRENT_SEQ=0
else
    # Extraemos el número de secuencia actual
    CURRENT_SEQ=$(peer lifecycle chaincode querycommitted --channelID mychannel --name basic | grep "Sequence" | sed -n 's/.*Sequence: \([0-9]*\).*/\1/p' | head -n 1)
    if [ -z "$CURRENT_SEQ" ]; then CURRENT_SEQ=0; fi
fi

# Calculamos la siguiente
NEXT_SEQ=$((CURRENT_SEQ + 1))
NEXT_VERSION="${VERSION_LABEL}.${NEXT_SEQ}"

echo "Secuencia Actual: $CURRENT_SEQ | Próxima Secuencia: $NEXT_SEQ"
echo "Versión a desplegar: $NEXT_VERSION"

# 3. Verificar si la red está arriba (por los contenedores)
if [ -n "$(docker ps -q -f name=peer0.org1.example.com)" ]; then
    echo "--- Red detectada. Ejecutando actualización... ---"
else
    echo "Red apagada. Iniciando red desde cero..."
    ./network.sh up createChannel -s couchdb
fi

# 4. Ejecutar el deploy de Hyperledger
# Nota: network.sh busca internamente 'organizations', como ya estamos en 'test-network', lo encontrará bien.
./network.sh deployCC -ccn basic -ccv $NEXT_VERSION -ccs $NEXT_SEQ -ccp ../../chaincode-go -ccl go

# 5. Levantar Java API
cd ..
echo "--- Reiniciando Java API ---"
docker-compose up -d --build

echo "sDespliegue Exitoso: v$NEXT_VERSION (Seq $NEXT_SEQ)"