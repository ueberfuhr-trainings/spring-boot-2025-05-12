package de.schulung.spring.customers.boundary.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

@TestConfiguration
class CustomerApiTestClientConfiguration {

  @Bean
  CustomerApiTestClient customerApiTestClient(MockMvc mvc) {
    return new CustomerApiTestClient(mvc);
  }

}
