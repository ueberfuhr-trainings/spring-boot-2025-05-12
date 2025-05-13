package de.schulung.spring.customers;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class Customer {

  private UUID uuid;
  private String name;
  private LocalDate birthdate;
  private String state = "active";


}
