package org.mukulphougat.presencesocketserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PresenceSocketServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PresenceSocketServerApplication.class, args);
	}

}
