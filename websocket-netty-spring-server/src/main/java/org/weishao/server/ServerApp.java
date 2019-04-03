package org.weishao.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ServerApp 
{
    public static void main( String[] args ){
        SpringApplication.run(ServerApp.class,args);
    }
}
