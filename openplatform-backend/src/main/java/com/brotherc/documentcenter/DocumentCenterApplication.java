package com.brotherc.documentcenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class DocumentCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentCenterApplication.class, args);
    }

}
