package de.schulung.spring.customers.boundary.testsupport;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({
  ElementType.TYPE,
  ElementType.ANNOTATION_TYPE
})
@AutoConfigureMockMvc
@Import(CustomerApiTestClientConfiguration.class)
public @interface AutoConfigureCustomerApiTestClient {

}
