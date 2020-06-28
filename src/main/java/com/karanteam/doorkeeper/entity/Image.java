package com.karanteam.doorkeeper.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Image {

    public static final String OFFICE = "OFFICE";

    @Id
    @GeneratedValue
    private int id;
    @Lob
    @NonNull
    private byte[] content;
    @NonNull
    private String key;

}
