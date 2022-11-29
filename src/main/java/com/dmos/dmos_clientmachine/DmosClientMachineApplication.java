package com.dmos.dmos_clientmachine;

import com.dmos.dmos_clientmachine.component.DMOSMachineClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DmosClientMachineApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(DmosClientMachineApplication.class).web(WebApplicationType.NONE).run(args);
        DMOSMachineClient.getSingle().run();
    }

}
