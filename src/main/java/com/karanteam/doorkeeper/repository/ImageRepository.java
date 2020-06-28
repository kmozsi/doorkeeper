package com.karanteam.doorkeeper.repository;

import com.karanteam.doorkeeper.entity.Image;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

  Optional<Image> findByKey(final String key);

}
