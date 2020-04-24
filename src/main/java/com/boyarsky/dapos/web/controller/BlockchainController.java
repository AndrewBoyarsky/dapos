package com.boyarsky.dapos.web.controller;

import com.boyarsky.dapos.core.DPoSApp;
import com.boyarsky.dapos.core.service.Blockchain;
import com.boyarsky.dapos.web.API;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping(API.REST_ROOT_URL + "/blockchain")
public class BlockchainController {
    private final PersistentEntityStore store;
    private final DPoSApp app;
    private Blockchain blockchain;

    @Autowired
    public BlockchainController(Blockchain blockchain, PersistentEntityStore store, DPoSApp app) {
        this.blockchain = blockchain;
        this.store = store;
        this.app = app;
    }

    @GetMapping("/status")
    public ResponseEntity<?> getLastBlock() {
        return ResponseEntity.of(Optional.ofNullable(blockchain.getLastBlock()));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> doRest() throws URISyntaxException, IOException, InterruptedException {
        URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
        Path path = Paths.get(location.toURI());
        Path runFrom = null;
        if (path.endsWith("java/main/")) {
            runFrom = path.getParent().getParent().getParent().getParent();
        } else if (path.endsWith(".jar")) {
            runFrom = path.getParent().getParent();
        } else {
            throw new RuntimeException("Uresolved codelocation: " + path);
        }
        runFrom = runFrom.resolve("run-node.sh");
        app.setAcceptRequest(false); // disable node
        store.clear();
        app.setAcceptRequest(true);
        Process exec = Runtime.getRuntime().exec("/bin/bash " + runFrom);
        int code = exec.waitFor();
        return ResponseEntity.ok(code);
    }
}
