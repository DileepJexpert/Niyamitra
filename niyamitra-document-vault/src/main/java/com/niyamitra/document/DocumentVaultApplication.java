package com.niyamitra.document;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.niyamitra.document", "com.niyamitra.common"})
public class DocumentVaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentVaultApplication.class, args);
    }
}
