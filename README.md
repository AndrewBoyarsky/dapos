# Dapos
Welcome! This repository store DPoS blockchain system running on Tendermint Core written in Java utilizing grpc interface.

Software Specs:
 * Db engine: Jetbrains Xodus
 * Main Framework: Spring Boot
 * Web server: Tomcat Embedded
 * Type of api: REST
 
 Features (planned, in-development):
  * Multisignature accounts/transactions
  * Two types of accounts (bitcoin|ethereum-compatible)
  * Delegation/unbonding of stakes
  * Smart contracts on JS 
  * Zero-fee transactions (Fee providers)
  * Encrypted messaging
  * Assets tokenization
  * Multisend transactions
  * Scaled by height blockchain config (different block time, tps, block size and block max gas)
  
  # How to run
   1. Install java 11 from [official java site](https://jdk.java.net/java-se-ri/11). Set `JAVA_HOME` env variable to point on `jdk` folder, Set `PATH` env variable to point on `jdk/bin` folder. 
   2. Check your java installed: `java -version`. Sample output: 
      ```
            openjdk version "11.0.6" 2020-01-14
            OpenJDK Runtime Environment GraalVM CE 19.3.1 (build 11.0.6+9-jvmci-19.3-b07)
            4OpenJDK 64-Bit Server VM GraalVM CE 19.3.1 (build 11.0.6+9-jvmci-19.3-b07, mixed mode, sharing)
      ```
   3. Clone this repository `master` branch.
   4. Clone tendermint core repository from [tendermint repo](https://github.com/tendermint/tendermint)
   5. Install go from the [official site](https://golang.org/dl/)
   6. Check your go installation by executing `go version` 
   Sample output: ```go version go1.14 linux/amd64```
   7. Install `make`, for Ubuntu `sudo apt-get install make`
   8. Go to the tendermint root directory and perform `git checkout v0.32.8` - last stable version, which works with java grpc.
   9. Then execute `make tools` then `make install` the `make build`
   10. Execute `tendermint version` . Output MUST BE: `0.32.8-14e04f76`, reinstall tendermint in other cases, do not proceed, since project will not be operational.
   11. Go to the `dapos` root directory and execute `./gradlew bootRun`
   12. Init tendermint and start node: `tendermint init`, then `tendermint node --abci grpc --proxy_app tcp://127.0.0.1:26658`
   13. You've done. Enjoy!
