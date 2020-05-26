package com.boyarsky.dapos.core;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class GrpcServer {
    private Server server;

    @Autowired
    public GrpcServer(BindableService service, @Value("${tendermint.abci.port}") int port) {
        this.server = ServerBuilder.forPort(port)
                .addService(service)
                .executor(Executors.newSingleThreadExecutor()) // replace by multi threaded without transaction manager
                .build();
    }


    @PostConstruct
    public void start() throws IOException {
        server.start();
        log.info("gRPC server started, listening on port " + server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("shutting down gRPC server since JVM is shutting down");
            stop();
            log.info("server shut down");
        }));
    }

    private void stop() {
        server.shutdown();
    }
}