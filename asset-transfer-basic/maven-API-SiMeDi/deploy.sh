#!/bin/bash
set -e

# Valores por defecto
VERSION=${1:-"1.0"}
SEQUENCE=${2:-"1"}

echo "--- Iniciando proceso de Deploy ---"
echo "--- Versión objetivo: $VERSION | Secuencia: $SEQUENCE ---"

# 1. Entrar al directorio test-network
cd test-network

# 2. Verificar si la red ya está arriba
# CORRECCIÓN: Usamos comillas dobles "$(...)" y -n para evitar errores de sintaxis
if [ -n "$(docker ps -q -f name=peer0.org1.example.com)" ]; then
    echo "La red ya está corriendo. OMITIENDO creación de canal."
else
    echo "La red no está detectada. Iniciando red y creando canal..."
    # Solo si no existe, levantamos todo
    ./network.sh up createChannel -s couchdb
fi

echo "--- Desplegando/Actualizando Chaincode ---"
# Importante: Si la red ya existe, esto actualiza el contrato
./network.sh deployCC -ccn basic -ccv $VERSION -ccs $SEQUENCE -ccp ../../chaincode-go -ccl go

# 3. Volver y levantar Java API
cd ..
echo "--- Reiniciando Java API ---"
docker-compose up -d --build

echo "--- Proceso finalizado. Código actualizado a v$VERSION ---"