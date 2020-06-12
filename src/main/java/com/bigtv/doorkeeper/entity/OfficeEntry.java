package com.bigtv.doorkeeper.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;

@Entity
@Data
public class OfficeEntry {

  @Id
  private String userId;
  private int ordinal;
  private boolean entered;

}
