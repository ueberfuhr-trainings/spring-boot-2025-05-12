package de.schulung.spring.customers.boundary;

import de.schulung.spring.customers.boundary.testsupport.AutoConfigureCustomerApiTestClient;
import de.schulung.spring.customers.boundary.testsupport.CustomerApiTestClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureCustomerApiTestClient
class CustomerApiTests {

  @Autowired
  CustomerApiTestClient testClient;

  // GET /customers -> 200
  @Test
  void shouldGetCustomers() {
    var newCustomer = testClient
      .requestCreateCustomerAndReturn();

    testClient
      .requestGetCustomersAndExpectToContainCustomer(newCustomer);

  }

  // GET /customers -> 406
  @Test
  void shouldNotGetCustomersAsXml() throws Exception {
    testClient
      .requestGetCustomers(
        req -> req.accept(MediaType.APPLICATION_XML)
      )
      .andExpect(status().isNotAcceptable());
  }

  @Test
  void shouldNotGetCustomersWithInvalidStateParameter() throws Exception {
    testClient
      .requestGetCustomers(
        req -> req.state("gelbekatze")
      )
      .andExpect(status().isBadRequest());
  }

  @RequiredArgsConstructor
  @Getter
  private enum StateParameter {
    ACTIVE("active"),
    LOCKED("locked"),
    DISABLED("disabled");
    private final String parameterValue;
  }

  @ParameterizedTest
  @EnumSource(StateParameter.class)
  void shouldGetCustomersByState(StateParameter parameter) {
    var newCustomer = testClient
      .requestCreateCustomerAndReturn(
        customer -> customer.state(parameter.getParameterValue())
      );

    // we need to find it with the state parameter
    testClient
      .requestGetCustomersAndExpectToContainCustomer(
        req -> req.state(parameter.getParameterValue()),
        newCustomer
      );
    // we must not find it with other state parameters
    for (StateParameter p : StateParameter.values()) {
      if (p != parameter) {
        testClient
          .requestGetCustomersAndExpectNotToContainCustomer(
            req -> req.state(p.getParameterValue()),
            newCustomer
          );
      }
    }

  }

  @Test
  void shouldCreateReturnAndDeleteCustomer() throws Exception {
    var newCustomer = testClient
      .requestCreateCustomerAndReturn();

    assertThat(newCustomer.getLocation())
      .isNotBlank();

    // read customer - 200
    testClient
      .getMvc()
      .perform(
        get(newCustomer.getLocation())
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.name").value("Tom Mayer"))
      .andExpect(jsonPath("$.birthdate").value("2005-05-12"))
      .andExpect(jsonPath("$.state").value("active"));

    // delete customer - 204
    testClient
      .getMvc()
      .perform(
        delete(newCustomer.getLocation())
      )
      .andExpect(status().isNoContent());

    // delete customer again - 404
    testClient
      .getMvc()
      .perform(
        delete(newCustomer.getLocation())
      )
      .andExpect(status().isNotFound());

    // read customer - 404
    testClient
      .getMvc()
      .perform(
        get(newCustomer.getLocation())
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound());

  }

  @Test
  void shouldNotCreateCustomerAsXml() throws Exception {
    testClient
      .requestCreateCustomer(
        req -> req
          .contentType(MediaType.APPLICATION_XML)
          .body("<customer/>")
      )
      .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  void shouldNotCreateCustomerAndReturnXml() throws Exception {
    testClient
      .requestCreateCustomer(
        req -> req.accept(MediaType.APPLICATION_XML)
      )
      .andExpect(status().isNotAcceptable());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    // missing comma
    """
      {
         "name": "Tom Mayer"
         "birthdate": "2005-05-12",
         "state": "active"
      }
      """,
    // invalid date
    """
      {
         "name": "Tom Mayer",
         "birthdate": "gelbekatze",
         "state": "active"
      }
      """,
    // invalid state
    """
      {
         "name": "Tom Mayer",
         "birthdate": "2005-05-12",
         "state": "gelbekatze"
      }
      """,
    // missing birthdate date
    """
      {
         "name": "Tom Mayer",
         "state": "active"
      }
      """,
    // missing name
    """
      {
         "birthdate": "2005-05-12",
         "state": "active"
      }
      """,
    // with uuid
    """
      {
         "uuid": "bf7f440b-c9de-4eb8-91f4-43108277e9a3",
         "name": "Tom Mayer",
         "birthdate": "2005-05-12",
         "state": "active"
      }
      """,
  })
  void shouldNotCreateInvalidCustomer(String body) throws Exception {
    testClient
      .requestCreateCustomer(
        req -> req.body(body)
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  void shouldCreateCustomerWithDefaultState() {
    var newCustomer = testClient
      .requestCreateCustomerAndReturn(
        customer -> customer.state(null)
      );
    assertThat(newCustomer.getResult().getState())
      .isNotNull()
      .isEqualTo("active");
  }

}
