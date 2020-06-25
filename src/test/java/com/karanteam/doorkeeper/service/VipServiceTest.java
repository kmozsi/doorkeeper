package com.karanteam.doorkeeper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.karanteam.doorkeeper.entity.Vip;
import com.karanteam.doorkeeper.repository.VipRepository;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class VipServiceTest {

    private static final String USER_ID = "USER_ID";

    @Autowired
    private VipService vipService;

    @MockBean
    private VipRepository vipRepository;

    private static final Vip VIP_USER = new Vip(USER_ID);

    private void evictCache() {
        when(vipRepository.findAll()).thenReturn(Collections.emptyList());
    }

    @Test
    public void testCacheWorking() {
        evictCache();

        when(vipRepository.findAll()).thenReturn(Collections.singletonList(VIP_USER));

        vipService.isVip(USER_ID);
        vipService.isVip(USER_ID);
        vipService.isVip(USER_ID);
        vipService.isVip(USER_ID);

        verify(vipRepository, times(1)).findAll();
    }

    @Test
    public void addVipUser() {
        when(vipRepository.save(any())).thenReturn(VIP_USER);
        vipService.addVipUser(USER_ID);
        verify(vipRepository, times(1)).save(VIP_USER);
    }

    @Test
    public void deleteVipUser() {
        when(vipRepository.findByUserId(any())).thenReturn(Optional.of(VIP_USER));
        boolean result = vipService.deleteVip(USER_ID);
        verify(vipRepository, times(1)).delete(VIP_USER);
        Assertions.assertTrue(result);
    }

    @Test
    public void deleteVipUserFails() {
        when(vipRepository.findByUserId(any())).thenReturn(Optional.empty());
        boolean result = vipService.deleteVip(USER_ID);
        verify(vipRepository, never()).delete(any());
        Assertions.assertFalse(result);
    }

    @Test
    public void getVipUsers() {
        when(vipRepository.findAll()).thenReturn(Collections.singletonList(VIP_USER));
        vipService.getVips();
        verify(vipRepository, times(1)).findAll();
    }

}
