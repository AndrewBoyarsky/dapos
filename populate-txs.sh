#!/bin/bash
# to self
curl -X POST "http://localhost:8888/api/rest/v1/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd\",\"pass\":\"12345\",\"recipient\":null,\"amount\":10,\"messageData\" : { \"message\":\"Just a first note\",\"isToSelf\":true}}"
curl -X POST "http://localhost:8888/api/rest/v1/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd\",\"pass\":\"12345\",\"recipient\":null,\"amount\":10,\"messageData\" : { \"message\":\"Hello, its just a second note\",\"isToSelf\":true}}"
curl -X POST "http://localhost:8888/api/rest/v1/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd\",\"pass\":\"12345\",\"recipient\":\"dbt1PQo75JgkzUhxbSmKaHrpEdKXfuev3ecZo\",\"messageData\" : { \"message\":\"Hello, dbt1PQo75JgkzUhxbSmKaHrpEdKXfuev3ecZo 1\",\"isToSelf\":false}}"
curl -X POST "http://localhost:8888/api/rest/v1/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd\",\"pass\":\"12345\",\"recipient\":\"dbt1PQo75JgkzUhxbSmKaHrpEdKXfuev3ecZo\",\"messageData\" : { \"message\":\"Hello, dbt1PQo75JgkzUhxbSmKaHrpEdKXfuev3ecZo 2\",\"isToSelf\":false}}"
curl -X POST "http://localhost:8888/api/rest/v1/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"dbt1PQo75JgkzUhxbSmKaHrpEdKXfuev3ecZo\",\"pass\":\"12345\",\"recipient\":\"det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd\",\"messageData\" : { \"message\":\"Hi, how are you?\",\"isToSelf\":false}}"
curl -X POST "http://localhost:8888/api/rest/v1/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd\",\"pass\":\"12345\",\"recipient\":\"dbt1PQo75JgkzUhxbSmKaHrpEdKXfuev3ecZo\",\"messageData\" : { \"message\":\"Oh, i am ok!\",\"isToSelf\":false}}"
curl -X POST "http://localhost:8888/api/rest/v1/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd\",\"pass\":\"12345\",\"recipient\":\"det0x6dc1df48f65bddc335917dc638d2981980409ee9\",\"amount\":10000,\"messageData\" : { \"message\":\"First money transfer\",\"isToSelf\":true}}"
curl -X POST "http://localhost:8888/api/rest/v1/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x6dc1df48f65bddc335917dc638d2981980409ee9\",\"pass\":\"12345\",\"recipient\":\"det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd\",\"amount\":1,\"messageData\" : { \"message\":\"Thank you for 1000 dapos\",\"isToSelf\":false}}"
curl -X POST "http://localhost:8888/api/rest/v1/payments" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x6dc1df48f65bddc335917dc638d2981980409ee9\",\"pass\":\"12345\",\"recipient\":\"det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd\",\"amount\":1,\"messageData\" : { \"message\":\"Just a test\",\"isToSelf\":false}}"
curl -X POST "http://localhost:8888/api/rest/v1/messages" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x6dc1df48f65bddc335917dc638d2981980409ee9\",\"pass\":\"12345\",\"recipient\":\"det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd\",\"messageData\" : { \"message\":\"Just a test\",\"isToSelf\":true}}"
curl -X POST "http://localhost:8888/api/rest/v1/fee-providers" -H "accept: */*" -H "Content-Type: application/json" -d "{\"account\":\"det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd\",\"pass\":\"12345\",\"recipient\":null,\"amount\":7777777777,\"feeProvider\":null,\"data\":null,\"fromConfig\":{\"rootConfig\":null,\"configGroups\":[{\"groupConfig\":{\"operations\":1,\"maxAllowedFee\":10000,\"totalAllowedFee\":15000,\"types\":[\"PAYMENT\",\"MESSAGE\",\"VOTE\",\"REVOKE\"]},\"accounts\":[\"det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd\",\"dap25e7f6efded119203e1b1bf9a0a72f02ac\"]}]},\"toFeeConfig\":{\"rootConfig\":null,\"configGroups\":null}}"
IFS='' read -r -d '' feeProv <<'EOF'
{ "account": "det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd",
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
{ "account": "det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd",
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
          "det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd", "dap25e7f6efded119203e1b1bf9a0a72f02ac", "dap2547fc3f49714b2db7acd5e18ae6a37110", "dbt1PQo75JgkzUhxbSmKaHrpEdKXfuev3ecZo", "det0x6dc1df48f65bddc335917dc638d2981980409ee9"
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
           "det0x8c09af0e77232e0ce6910ccf10b4037c0225eedd", "dap25e7f6efded119203e1b1bf9a0a72f02ac"
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
           "dap2547fc3f49714b2db7acd5e18ae6a37110", "dbt1PQo75JgkzUhxbSmKaHrpEdKXfuev3ecZo", "det0x6dc1df48f65bddc335917dc638d2981980409ee9"
        ]
      }
    ]
  }
}
EOF

curl -X POST "http://localhost:8888/api/rest/v1/fee-providers" -H "accept: */*" -H "Content-Type: application/json" -d "$feeProv"
