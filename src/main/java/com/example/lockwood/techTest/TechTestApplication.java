package com.example.lockwood.techTest;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import static org.springframework.boot.SpringApplication.run;

@ServletComponentScan
@SpringBootApplication
public class TechTestApplication {

	public static void main(String[] args) {
		run(TechTestApplication.class, args);
	}

}
