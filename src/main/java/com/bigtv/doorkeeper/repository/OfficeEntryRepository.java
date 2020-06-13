package com.bigtv.doorkeeper.repository;

import com.bigtv.doorkeeper.entity.OfficeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficeEntryRepository extends JpaRepository<OfficeEntry, String> {

}
