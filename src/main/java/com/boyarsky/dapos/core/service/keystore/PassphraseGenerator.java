package com.boyarsky.dapos.core.service.keystore;

public interface PassphraseGenerator {
    /**
     * Generate string(passphrase) which consist of random words separated by space
     * @return generated passphrase
     */
    String generate();
}

