package org.ject.support.common.security.config;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.ject.support.testconfig.ApplicationPeriodTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false"})
class SecurityConfigTest extends ApplicationPeriodTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleHierarchy roleHierarchy;

    @Test
    @DisplayName("RoleHierarchy가 올바르게 설정되었는지 확인")
    void roleHierarchy_ShouldBeConfiguredCorrectly() {
        // given
        SimpleGrantedAuthority tempAuthority = new SimpleGrantedAuthority("ROLE_TEMP");
        SimpleGrantedAuthority userAuthority = new SimpleGrantedAuthority("ROLE_USER");
        SimpleGrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        SimpleGrantedAuthority verificationAuthority = new SimpleGrantedAuthority("ROLE_VERIFICATION");

        // when & then
        // ADMIN > USER > TEMP 계층 구조 확인
        List<String> adminAuthorities = roleHierarchy.getReachableGrantedAuthorities(
                java.util.Collections.singleton(adminAuthority))
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        
        assertThat(adminAuthorities).contains(
                adminAuthority.getAuthority(), 
                userAuthority.getAuthority(), 
                tempAuthority.getAuthority());

        // USER > TEMP 계층 구조 확인
        List<String> userAuthorities = roleHierarchy.getReachableGrantedAuthorities(
                java.util.Collections.singleton(userAuthority))
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
                
        assertThat(userAuthorities)
                .contains(userAuthority.getAuthority(), tempAuthority.getAuthority())
                .doesNotContain(adminAuthority.getAuthority());

        // TEMP > VERIFICATION 계층 구조 확인
        List<String> tempAuthorities = roleHierarchy.getReachableGrantedAuthorities(
                java.util.Collections.singleton(tempAuthority))
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
                
        assertThat(tempAuthorities)
                .contains(tempAuthority.getAuthority(), verificationAuthority.getAuthority())
                .doesNotContain(adminAuthority.getAuthority(), userAuthority.getAuthority());
    }

    @Test
    @DisplayName("permitAll 설정이 적용된 경로는 인증 없이 접근 가능해야 함")
    void permitAll_ShouldAllowAccessWithoutAuthentication() throws Exception {
        // 인증 없이 접근 가능한지 확인 (SecurityConfig에서 .anyRequest().permitAll() 설정)
        mockMvc.perform(get("/any-path"))
                .andExpect(status().isOk()); // 404는 경로가 없어서이지, 인증 실패(401, 403)가 아님
    }
}
