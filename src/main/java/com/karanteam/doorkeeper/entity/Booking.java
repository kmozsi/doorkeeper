package com.karanteam.doorkeeper.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
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
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ordinal_seq")
    private Integer ordinal;

    private String userId;
    private boolean entered = false;
    private boolean exited = false;
    @OneToOne
    private OfficePosition officePosition;
}
