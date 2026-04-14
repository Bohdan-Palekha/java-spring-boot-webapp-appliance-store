package com.epam.rd.autocode.assessment.appliances;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
@Slf4j
public class ApplianceStoreSpringApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ApplianceStoreSpringApplication.class, args);
        Environment env = ctx.getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String profile = String.join(", ", env.getActiveProfiles().length > 0
                ? env.getActiveProfiles() : new String[]{"default"});
        log.info("\n ─── Appliance Store started ───\n http://localhost:{}\n Profile: {}", port, profile);
    }
}
