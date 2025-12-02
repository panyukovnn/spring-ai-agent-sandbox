package ru.panyukovnn.springaiagentsandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.shell.command.annotation.CommandScan;

@CommandScan
@EnableFeignClients
@SpringBootApplication
public class SpringAiAgentSandboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiAgentSandboxApplication.class, args);
    }
}