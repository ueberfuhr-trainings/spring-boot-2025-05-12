package de.schulung.spring.customers.boundary;

import de.schulung.spring.customers.boundary.testsupport.AutoConfigureCustomerApiTestClient;
import de.schulung.spring.customers.boundary.testsupport.CustomerApiTestClient;
import de.schulung.spring.customers.domain.CustomersService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureCustomerApiTestClient
public class CustomerApiWithMockedServiceTests {

  @Autowired
  CustomerApiTestClient testClient;
  @MockitoBean
  CustomersService customersService;

  @Test
  void shouldReturnNotFoundWhenCustomerNotExists() throws Exception {

    var uuid = UUID.randomUUID();
    when(customersService.findById(uuid))
      .thenReturn(Optional.empty());

    testClient
      .requestGetCustomer(uuid.toString())
      .andExpect(status().isNotFound());

  }

  @Test
  void shouldNotCreateCustomerOnValidationError() throws Exception {
    testClient
      .requestCreateCustomer(
        req -> req.body(
          """
             {
                "birthdate": "2005-05-12",
               "state": "active"
            }
            """
        )
      )
      .andExpect(status().isBadRequest());

    verify(customersService, never()).create(any());

  }


}
