#!/bin/bash
# to self
curl -X POST "http://localhost:8080/api/rest/v1/txs/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"pass\":\"12345\",\"recipient\":null,\"amount\":10,\"feeProvider\":0,\"data\":\"Just a first note\",\"toSelf\":true}"
curl -X POST "http://localhost:8080/api/rest/v1/txs/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"pass\":\"12345\",\"recipient\":null,\"amount\":10,\"feeProvider\":0,\"data\":\"Hello, its just a second note\",\"toSelf\":true}"
curl -X POST "http://localhost:8080/api/rest/v1/txs/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"pass\":\"12345\",\"recipient\":\"1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT\",\"feeProvider\":0,\"data\":\"Hello, 1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT 1\",\"toSelf\":false}"
curl -X POST "http://localhost:8080/api/rest/v1/txs/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"pass\":\"12345\",\"recipient\":\"1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT\",\"feeProvider\":0,\"data\":\"Hello, 1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT 2\",\"toSelf\":false}"
curl -X POST "http://localhost:8080/api/rest/v1/txs/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT\",\"pass\":\"12345\",\"recipient\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"feeProvider\":0,\"data\":\"Hi, how are you?\",\"toSelf\":false}"
curl -X POST "http://localhost:8080/api/rest/v1/txs/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"pass\":\"12345\",\"recipient\":\"1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT\",\"feeProvider\":0,\"data\":\"Oh, i am ok!\",\"toSelf\":false}"
curl -X POST "http://localhost:8080/api/rest/v1/txs/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"pass\":\"12345\",\"recipient\":\"det0x45bb63cff2c5b2fe7d2eb229d5ffd45132876671\",\"amount\":10000,\"feeProvider\":0,\"data\":\"First money transfer\",\"toSelf\":true}"
curl -X POST "http://localhost:8080/api/rest/v1/txs/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x45bb63cff2c5b2fe7d2eb229d5ffd45132876671\",\"pass\":\"1234511\",\"recipient\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"amount\":1,\"feeProvider\":0,\"data\":\"Thank you for 1000 dapos\",\"toSelf\":false}"
curl -X POST "http://localhost:8080/api/rest/v1/txs/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x45bb63cff2c5b2fe7d2eb229d5ffd45132876671\",\"pass\":\"1234511\",\"recipient\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"amount\":1,\"feeProvider\":0,\"data\":\"Just a test\",\"toSelf\":false}"
curl -X POST "http://localhost:8080/api/rest/v1/txs/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x45bb63cff2c5b2fe7d2eb229d5ffd45132876671\",\"pass\":\"1234511\",\"recipient\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"amount\":0,\"feeProvider\":0,\"data\":\"Just a test\",\"toSelf\":true}"