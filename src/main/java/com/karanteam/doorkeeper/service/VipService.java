package com.karanteam.doorkeeper.service;

import static com.karanteam.doorkeeper.config.CachingConfig.POSITION_CACHE;

import com.karanteam.doorkeeper.config.CachingConfig;
import com.karanteam.doorkeeper.entity.Vip;
import com.karanteam.doorkeeper.repository.VipRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VipService {

    private final VipRepository vipRepository;

    public VipService(VipRepository vipRepository) {
        this.vipRepository = vipRepository;
    }

    @CachePut(value = POSITION_CACHE, key = "#userId")
    public void addVipUser(String userId) {
        vipRepository.save(new Vip(userId));
    }

    @CacheEvict(value = POSITION_CACHE, key = "#userId")
    public boolean deleteVip(String userId) {
        Optional<Vip> user = vipRepository.findByUserId(userId);
        user.ifPresent(vipRepository::delete);
        return user.isPresent();
    }

    public List<Vip> getVips() {
        return vipRepository.findAll();
    }

    public List<String> getVipUserIds() {
        return getVips().stream().map(Vip::getUserId).collect(Collectors.toList());
    }

    @Cacheable(value = CachingConfig.VIP_CACHE)
    public boolean isVip(String userId) {
        return getVipUserIds().contains(userId);
    }

}
