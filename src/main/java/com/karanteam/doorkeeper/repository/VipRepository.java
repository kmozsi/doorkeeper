package com.karanteam.doorkeeper.repository;

import com.karanteam.doorkeeper.entity.Vip;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VipRepository extends JpaRepository<Vip, Integer> {

  Optional<Vip> findByUserId(final String userId);

}
