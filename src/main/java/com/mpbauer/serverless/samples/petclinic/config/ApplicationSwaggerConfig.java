package com.mpbauer.serverless.samples.petclinic.config;


import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;

import javax.ws.rs.core.Application;

@OpenAPIDefinition(
    info = @Info(
        title = "REST Petclinic backend Api Documentation",
        description = "This is REST API documentation of the Spring Petclinic backend. If authentication is enabled, when calling the APIs use admin/admin",
        version = "1.0",
        contact = @Contact(
            name = "Vitaliy Fedoriv",
            url = "https://github.com/spring-petclinic/spring-petclinic-rest",
            email = "vitaliy.fedoriv@gmail.com"),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"))
)
public class ApplicationSwaggerConfig extends Application {
}
