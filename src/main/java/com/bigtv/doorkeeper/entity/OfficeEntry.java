package com.bigtv.doorkeeper.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class OfficeEntry {

  @Id
  private String userId;
  private int ordinal;
  private boolean entered;

}
