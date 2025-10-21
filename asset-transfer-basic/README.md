# Asset transfer basic sample

The asset transfer basic sample demonstrates:

- Connecting a client application to a Fabric blockchain network.
- Submitting smart contract transactions to update ledger state.
- Evaluating smart contract transactions to query ledger state.
- Handling errors in transaction invocation.

## About the sample

This sample includes smart contract and application code in multiple languages. This sample shows create, read, update, transfer and delete of an asset.

For a more detailed walk-through of the application code and client API usage, refer to the [Running a Fabric Application tutorial](https://hyperledger-fabric.readthedocs.io/en/latest/write_first_app.html) in the main Hyperledger Fabric documentation.

### Application

Follow the execution flow in the client application code, and corresponding output on running the application. Pay attention to the sequence of:

- Transaction invocations (console output like "**--> Submit Transaction**" and "**--> Evaluate Transaction**").
- Results returned by transactions (console output like "**\*\*\* Result**").

### Smart Contract

The smart contract (in folder `chaincode-xyz`) implements the following functions to support the application:

- CreateAsset
- ReadAsset
- UpdateAsset
- DeleteAsset
- TransferAsset

Note that the asset transfer implemented by the smart contract is a simplified scenario, without ownership validation, meant only to demonstrate how to invoke transactions.

## Running the sample

The Fabric test network is used to deploy and run this sample. Follow these steps in order:

Levantar hyperledger + api (en testing):
(requiere comentar external: true en docker en el docker compose de la api)
docker-compose \
  -f docker-compose.yaml \
  -f test-network/compose/compose-test-net.yaml \
  -f test-network/compose/compose-couch.yaml \
  up -d --build

Luego configurar red:
  cd test-network/
  ./network.sh up createChannel -s couchdb
  ./network.sh deployCC -ccn basic -ccv 1.0 -ccp ../../chaincode-go -ccl go

1. Create the test network and a channel (from the `test-network` folder).

   ```
   Standard:
    ./network.sh up createChannel -c mychannel -ca
   
   CouchDb:
    ./network.sh up createChannel -s couchdb
   ```

1. Deploy one of the smart contract implementations (from the `test-network` folder).

   - To deploy the **TypeScript** chaincode implementation:

     ```shell
     ./network.sh deployCC -ccn basic -ccp ../asset-transfer-basic/chaincode-typescript/ -ccl typescript
     ```

   - To deploy the **JavaScript** chaincode implementation:

     ```shell
     ./network.sh deployCC -ccn basic -ccp ../asset-transfer-basic/chaincode-javascript/ -ccl javascript
     ```

   - To deploy the **Go** chaincode implementation:

     ```shell
     Standard:
     ./network.sh deployCC -ccn basic -ccp ../asset-transfer-basic/chaincode-go/ -ccl go
     
     CouchDb:
     ./network.sh deployCC -ccn basic -ccv 1.0 -ccp ../asset-transfer-basic/chaincode-go -ccl go

     ./network.sh deployCC -ccn basic -ccv 1.0 -ccp ../../chaincode-go -ccl go
     ```

   - To deploy the **Java** chaincode implementation:
     ```shell
     ./network.sh deployCC -ccn basic -ccp ../asset-transfer-basic/chaincode-java/ -ccl java
     ```

1. Run the application (from the `asset-transfer-basic` folder).

   - To run the **TypeScript** sample application:

     ```shell
     cd application-gateway-typescript
     npm install
     npm start
     ```

   - To run the **JavaScript** sample application:

     ```shell
     cd application-gateway-javascript
     npm install
     npm start
     ```

   - To run the **Go** sample application:

     ```shell
     cd application-gateway-go
     go run .
     ```

   - To run the **Java** sample application:
     ```shell
     cd application-gateway-java
     ./gradlew run
     ```

     - To run the SiMeDi **Java** sample application:
     ```shell
     cd maven-API-SiMeDi
      ./mvnw spring-boot:run

      con docker:
      docker-compose up -d --build
     ```

## Clean up

When you are finished, you can bring down the test network (from the `test-network` folder). The command will remove all the nodes of the test network, and delete any ledger data that you created.

```shell
./network.sh down
```
