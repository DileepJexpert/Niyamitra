package com.niyamitra.kavach.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.niyamitra.kavach.agent", "com.niyamitra.common"})
public class KavachFloorManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KavachFloorManagerApplication.class, args);
    }
}
