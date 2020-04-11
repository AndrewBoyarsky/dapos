package com.boyarsky.dapos.core.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoredWallet {
    private String account;
    private String publicKey;
    private String encryptedPrivateKey;
    private String cryptoAlgo;
    private String mac;
    private LocalDateTime time = LocalDateTime.now();

    public String getTime() {
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public void setTime(String time) {
        this.time = LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(time));
    }
}
