package com.progIII.ms_security.Configurations;

import com.progIII.ms_security.Interceptors.SecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private SecurityInterceptor securityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //Este código son las puertas de un muro
        registry.addInterceptor(securityInterceptor)
                .addPathPatterns("/api/**") //esta linea es la que activa la seguridad
                .excludePathPatterns("/api/public/**"); //esta linea es la exclusión a dicha seguridad


    }
}