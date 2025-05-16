package de.schulung.spring.customers.boundary.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RequiredArgsConstructor
public class CustomerApiTestClient {

  // do not inject configured ObjectMapper from Spring context
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Getter
  private final MockMvc mvc;

  @Builder
  @Getter
  public static class CustomerInput {
    @Builder.Default
    private final String name = "Tom Mayer";
    @Builder.Default
    private final String birthdate = "2005-05-12";
    @Builder.Default
    private final String state = "active";
  }

  @Builder
  @Getter
  public static class CreateCustomerOptions {
    @Builder.Default
    private final MediaType accept = MediaType.APPLICATION_JSON;
    @Builder.Default
    private final MediaType contentType = MediaType.APPLICATION_JSON;
    private final String body;
  }

  @Builder
  @Getter
  public static class CustomerResult {
    private final String uuid;
    private final String name;
    private final String birthdate;
    private final String state;
  }

  @Builder
  @Getter
  public static class CreateCustomerResponse {
    private final String location;
    private final CustomerInput input;
    private final CustomerResult result;
  }

  @SneakyThrows
  public CreateCustomerResponse requestCreateCustomerAndReturn() {
    return requestCreateCustomerAndReturn(
      Function.identity()
    );
  }

  @SneakyThrows
  public CreateCustomerResponse requestCreateCustomerAndReturn(
    Function<CustomerInput.CustomerInputBuilder, CustomerInput.CustomerInputBuilder> inputOptions
  ) {
    final var input = inputOptions
      .apply(
        CustomerInput
          .builder()
      )
      .build();
    var response = requestCreateCustomer(
      inputOptions,
      Function.identity()
    )
      .andExpect(status().isCreated())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.name").value(input.getName()))
      .andExpect(jsonPath("$.birthdate").value(input.getBirthdate()))
      .andExpect(
        input.getState() != null
          ? jsonPath("$.state").value(input.getState())
          : result -> {
        }
      )
      .andExpect(jsonPath("$.uuid").isNotEmpty())
      .andExpect(header().exists("Location"))
      .andReturn()
      .getResponse();
    var tree = objectMapper
      .readTree(response.getContentAsString());
    return CreateCustomerResponse
      .builder()
      .input(input)
      .location(
        response
          .getHeader(HttpHeaders.LOCATION)
      )
      .result(
        CustomerResult
          .builder()
          .uuid(tree.path("uuid").asText())
          .name(tree.path("name").asText())
          .birthdate(tree.path("birthdate").asText())
          .state(tree.path("state").asText())
          .build()
      )
      .build();
  }

  @SneakyThrows
  public ResultActions requestCreateCustomer(
    Function<CreateCustomerOptions.CreateCustomerOptionsBuilder, CreateCustomerOptions.CreateCustomerOptionsBuilder> options
  ) {
    return requestCreateCustomer(
      Function.identity(),
      options
    );
  }

  @SneakyThrows
  public ResultActions requestCreateCustomer(
    Function<CustomerInput.CustomerInputBuilder, CustomerInput.CustomerInputBuilder> inputOptions,
    Function<CreateCustomerOptions.CreateCustomerOptionsBuilder, CreateCustomerOptions.CreateCustomerOptionsBuilder> options
  ) {
    final var input = inputOptions
      .apply(
        CustomerInput
          .builder()
      )
      .build();
    final var postOptions = options
      .apply(
        CreateCustomerOptions
          .builder()
      )
      .build();
    return mvc.perform(
      post("/customers")
        .contentType(postOptions.getContentType())
        .content(
          null != postOptions.getBody()
            ? postOptions.getBody()
            : String.format(
            """
                {
                  %s
                  "name": "%s",
                  "birthdate": "%s"
                }
              """,
            (
              null != input.getState()
                ? String.format(
                """
                    "state": "%s",
                  """,
                input.getState()
              )
                : ""
            ),
            input.getName(),
            input.getBirthdate()
          )
        )
        .accept(postOptions.getAccept())
    );
  }

  @Builder
  @Getter
  public static class GetCustomersOptions {
    @Builder.Default
    private final MediaType accept = MediaType.APPLICATION_JSON;
    private final String state;
  }

  @SneakyThrows
  public ResultActions requestGetCustomers() {
    return requestGetCustomers(
      Function.identity()
    );
  }

  @SneakyThrows
  public ResultActions requestGetCustomers(
    Function<GetCustomersOptions.GetCustomersOptionsBuilder, GetCustomersOptions.GetCustomersOptionsBuilder> options
  ) {
    var requestOptions = options
      .apply(
        GetCustomersOptions
          .builder()
      )
      .build();
    return mvc
      .perform(
        (
          null != requestOptions.getState()
            ? get("/customers")
            .param("state", requestOptions.getState())
            : get("/customers")
        )
          .accept(requestOptions.getAccept())
      );
  }

  @SneakyThrows
  public ResultActions requestGetCustomersAndExpectToContainCustomer(
    CreateCustomerResponse createCustomerResponse
  ) {
    return requestGetCustomersAndExpectToContainCustomer(
      Function.identity(),
      createCustomerResponse
    );
  }

  @SneakyThrows
  public ResultActions requestGetCustomersAndExpectToContainCustomer(
    Function<GetCustomersOptions.GetCustomersOptionsBuilder, GetCustomersOptions.GetCustomersOptionsBuilder> options,
    CreateCustomerResponse createCustomerResponse
  ) {
    return requestGetCustomers(options)
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(
        jsonPath(
          String
            .format(
              "$[?(@.uuid == '%s' && @.name == '%s' && @.birthdate == '%s' && @.state == '%s')]",
              createCustomerResponse.getResult().getUuid(),
              createCustomerResponse.getInput().getName(),
              createCustomerResponse.getInput().getBirthdate(),
              createCustomerResponse.getInput().getState()
            )
        )
          .exists()
      );
  }

  @SneakyThrows
  public ResultActions requestGetCustomersAndExpectNotToContainCustomer(
    String uuid
  ) {
    return requestGetCustomersAndExpectNotToContainCustomer(
      Function.identity(),
      () -> uuid
    );
  }

  @SneakyThrows
  public ResultActions requestGetCustomersAndExpectNotToContainCustomer(
    Function<GetCustomersOptions.GetCustomersOptionsBuilder, GetCustomersOptions.GetCustomersOptionsBuilder> options,
    String uuid
  ) {
    return requestGetCustomersAndExpectNotToContainCustomer(
      options,
      () -> uuid
    );
  }

  @SneakyThrows
  public ResultActions requestGetCustomersAndExpectNotToContainCustomer(
    CreateCustomerResponse createCustomerResponse
  ) {
    return requestGetCustomersAndExpectNotToContainCustomer(
      Function.identity(),
      createCustomerResponse.getResult()::getUuid
    );
  }

  @SneakyThrows
  public ResultActions requestGetCustomersAndExpectNotToContainCustomer(
    Function<GetCustomersOptions.GetCustomersOptionsBuilder, GetCustomersOptions.GetCustomersOptionsBuilder> options,
    CreateCustomerResponse createCustomerResponse
  ) {
    return requestGetCustomersAndExpectNotToContainCustomer(
      options,
      createCustomerResponse.getResult()::getUuid
    );
  }

  @SneakyThrows
  private ResultActions requestGetCustomersAndExpectNotToContainCustomer(
    Function<GetCustomersOptions.GetCustomersOptionsBuilder, GetCustomersOptions.GetCustomersOptionsBuilder> options,
    Supplier<String> uuid
  ) {
    return requestGetCustomers(options)
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(
        jsonPath(
          String
            .format(
              "$[?(@.uuid == '%s')]",
              uuid.get()
            )
        )
          .doesNotExist()
      );
  }

  @SneakyThrows
  public ResultActions requestGetCustomer(String uuid) {
    return mvc
      .perform(
        get("/customers/{uuid}", uuid)
          .accept(MediaType.APPLICATION_JSON)
      );
  }

}
