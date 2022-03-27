package com.dam0.springbootelasticsearch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    /**
     * CORS 설정
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")   // -> 이거 CORS 에러남. 밑에 allowedOriginPatterns("*")로 고쳐야됨. 그 이유는 allowCredentials(true) 랑 상충한다고함.
//                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "HEAD", "OPTIONS", "POST", "PUT", "DELETE")
                        .allowCredentials(true);
            }
        };
    }

}
