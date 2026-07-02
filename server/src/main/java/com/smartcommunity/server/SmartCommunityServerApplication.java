package com.smartcommunity.server;

import com.smartcommunity.server.security.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@MapperScan("com.smartcommunity.server.modules")
public class SmartCommunityServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartCommunityServerApplication.class, args);
	}

}
