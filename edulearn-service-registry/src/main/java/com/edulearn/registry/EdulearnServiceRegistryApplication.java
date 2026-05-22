package com.edulearn.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EdulearnServiceRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdulearnServiceRegistryApplication.class, args);
	}

}
