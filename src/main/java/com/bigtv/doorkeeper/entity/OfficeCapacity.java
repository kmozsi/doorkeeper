package com.bigtv.doorkeeper.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class OfficeCapacity {
  // TODO validate values and maybe the daily capacity (capacity * allowedPercentage / 100 could be cached)
  @Id
  @GeneratedValue
  private Integer id;
  private Integer capacity;
  private Integer allowedPercentage;

}
