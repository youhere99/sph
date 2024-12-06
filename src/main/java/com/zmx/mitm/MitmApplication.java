package com.zmx.mitm;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MitmApplication {
    public static void main(String[] args){
        new SpringApplicationBuilder(MitmApplication.class).headless(false).run(args);
    }
}
