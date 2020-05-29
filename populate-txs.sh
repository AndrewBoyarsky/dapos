#!/bin/bash
# to self
curl -X POST "http://localhost:8888/api/rest/v1/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"pass\":\"12345\",\"recipient\":null,\"amount\":10,\"message\":\"Just a first note\",\"toSelf\":true}"
curl -X POST "http://localhost:8888/api/rest/v1/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"pass\":\"12345\",\"recipient\":null,\"amount\":10,\"message\":\"Hello, its just a second note\",\"toSelf\":true}"
curl -X POST "http://localhost:8888/api/rest/v1/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"pass\":\"12345\",\"recipient\":\"1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT\",\"message\":\"Hello, 1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT 1\",\"toSelf\":false}"
curl -X POST "http://localhost:8888/api/rest/v1/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"pass\":\"12345\",\"recipient\":\"1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT\",\"message\":\"Hello, 1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT 2\",\"toSelf\":false}"
curl -X POST "http://localhost:8888/api/rest/v1/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT\",\"pass\":\"12345\",\"recipient\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"message\":\"Hi, how are you?\",\"toSelf\":false}"
curl -X POST "http://localhost:8888/api/rest/v1/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"pass\":\"12345\",\"recipient\":\"1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT\",\"message\":\"Oh, i am ok!\",\"toSelf\":false}"
curl -X POST "http://localhost:8888/api/rest/v1/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"pass\":\"12345\",\"recipient\":\"det0x45bb63cff2c5b2fe7d2eb229d5ffd45132876671\",\"amount\":10000,\"message\":\"First money transfer\",\"toSelf\":true}"
curl -X POST "http://localhost:8888/api/rest/v1/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x45bb63cff2c5b2fe7d2eb229d5ffd45132876671\",\"pass\":\"1234511\",\"recipient\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"amount\":1,\"message\":\"Thank you for 1000 dapos\",\"toSelf\":false}"
curl -X POST "http://localhost:8888/api/rest/v1/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x45bb63cff2c5b2fe7d2eb229d5ffd45132876671\",\"pass\":\"1234511\",\"recipient\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"amount\":1,\"message\":\"Just a test\",\"toSelf\":false}"
curl -X POST "http://localhost:8888/api/rest/v1/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x45bb63cff2c5b2fe7d2eb229d5ffd45132876671\",\"pass\":\"1234511\",\"recipient\":\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"amount\":0,\"message\":\"Just a test\",\"toSelf\":true}"
curl -X POST "http://localhost:8888/api/rest/v1/fee-providers" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"pass\":\"12345\",\"recipient\":null,\"amount\":7777777777,\"feeProvider\":null,\"data\":null,\"fromConfig\":{\"rootConfig\":null,\"configGroups\":[{\"groupConfig\":{\"operations\":1,\"maxAllowedFee\":10000,\"totalAllowedFee\":15000,\"types\":[\"PAYMENT\",\"MESSAGE\",\"VOTE\",\"REVOKE\"]},\"accounts\":[\"det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d\",\"254d52eaf1114b8c68df983b0b272e4e2f\"]}]},\"toFeeConfig\":{\"rootConfig\":null,\"configGroups\":null},\"toSelf\":false}"
IFS='' read -r -d '' feeProv <<'EOF'
{ "account": "0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d",
  "pass": "12345",
  "recipient": null,
  "amount": 434545550000,
  "feeProvider": null,
  "data": null,
  "fromConfig": {
    "rootConfig": {
          "operations": 3,
          "maxAllowedFee": 50000,
          "totalAllowedFee": 100000,
          "types": []
    },
    "configGroups": null
  },
  "toFeeConfig": {
   "rootConfig": null,
    "configGroups": null
  }
}
EOF
curl -X POST "http://localhost:8888/api/rest/v1/fee-providers" -H "accept: */*" -H "Content-Type: application/json" -d "$feeProv"
IFS='' read -r -d '' feeProv <<'EOF'
{ "account": "0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d",
  "pass": "12345",
  "recipient": null,
  "amount": 44444444444,
  "feeProvider": null,
  "data": null,
  "fromConfig": {
    "rootConfig": null,
    "configGroups": [
      {
        "groupConfig": {
          "operations": 20,
          "maxAllowedFee": 250000,
          "totalAllowedFee": 1500000,
          "types": [
            "PAYMENT", "MESSAGE", "VOTE", "REVOKE", "CURRENCY_TRANSFER"
          ]
        },
        "accounts": [
          "det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d", "254d52eaf1114b8c68df983b0b272e4e2f", "2515e1d552eb5cf63ea58aca4654f5b12f", "1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT", "det0x45bb63cff2c5b2fe7d2eb229d5ffd45132876671"
        ]
      }
    ]
  },
  "toFeeConfig": {
   "rootConfig": null,
    "configGroups": [
      {
        "groupConfig": {
          "operations": -1,
          "maxAllowedFee": -1,
          "totalAllowedFee": -1,
          "types": [
            "PAYMENT"
          ]
        },
        "accounts": [
           "det0xf1ff0edb0326e6a224c07fed1bdad3cebad5758d", "254d52eaf1114b8c68df983b0b272e4e2f"
        ]
      },
      {
        "groupConfig": {
          "operations": 5,
          "maxAllowedFee": -1,
          "totalAllowedFee": 100000,
          "types": [
          ]
        },
        "accounts": [
           "2515e1d552eb5cf63ea58aca4654f5b12f", "1L5pPdFqgkRhuYhpBCZZ877qNHVYKT4VWT", "det0x45bb63cff2c5b2fe7d2eb229d5ffd45132876671"
        ]
      }
    ]
  }
}
EOF

curl -X POST "http://localhost:8888/api/rest/v1/fee-providers" -H "accept: */*" -H "Content-Type: application/json" -d "$feeProv"