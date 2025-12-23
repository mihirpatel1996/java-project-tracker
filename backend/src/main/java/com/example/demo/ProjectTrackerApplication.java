package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class ProjectTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectTrackerApplication.class, args);
	}

//	//	Global configuration of CORS
//	@Bean
//	public WebMvcConfigurer corsConfirgurer(){
//		return new WebMvcConfigurer() {
//			@Override
//			public void addCorsMappings(CorsRegistry registry) {
//				registry.addMapping("/")
//						.allowedOrigins("http://localhost:9000");
//			}
//		};
//	}
}
