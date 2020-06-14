package com.bigtv.doorkeeper.entity;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(name = "ordinal_seq", allocationSize = 1000)
public class OfficeEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ordinal_seq")
  private Integer ordinal;

  private String userId;
  private boolean entered = false;
  private boolean exited = false;

}
