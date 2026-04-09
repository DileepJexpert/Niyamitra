package com.niyamitra.anupalan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.niyamitra.anupalan", "com.niyamitra.common"})
public class AnupalanRuleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnupalanRuleServiceApplication.class, args);
    }
}
