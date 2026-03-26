package com.niyamitra.kavach.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.niyamitra.kavach.notification", "com.niyamitra.common"})
public class KavachNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(KavachNotificationApplication.class, args);
    }
}
