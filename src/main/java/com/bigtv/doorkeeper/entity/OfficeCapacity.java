package com.bigtv.doorkeeper.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Data;

@Entity
@Data
public class OfficeCapacity {
  // TODO validate values and maybe the daily capacity (capacity * allowedPercentage / 100 could be cached)
  @Id
  @GeneratedValue
  private int id;
  private int capacity;
  private int allowedPercentage;

}
