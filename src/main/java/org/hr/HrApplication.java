package org.hr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.logging.Logger;

/**
 * Punct de intrare Spring Boot pentru aplicatia HR (Sprint 2: Domain Implementation).
 * Configurarea beanurilor de infrastructura se afla in {@link org.hr.config.AppScanConfig}.
 */
@SpringBootApplication
public class HrApplication extends SpringBootServletInitializer {
    private static final Logger logger = Logger.getLogger(HrApplication.class.getName());

    public static void main(String[] args) {
        logger.info("Loading multistrat :: SpringBoot -> HrApplication Default Settings ...");
        SpringApplication.run(HrApplication.class, args);
    }
}
