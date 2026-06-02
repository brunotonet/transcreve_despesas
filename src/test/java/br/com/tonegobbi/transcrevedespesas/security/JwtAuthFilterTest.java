package br.com.tonegobbi.transcrevedespesas.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final FilterChain filterChain = mock(FilterChain.class);
    private final JwtAuthFilter filter = new JwtAuthFilter(jwtService, userDetailsService);

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void segueCadeiaSemAutenticarQuandoHeaderNaoFoiInformado() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void autenticaUsuarioQuandoBearerTokenEValido() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        var response = new MockHttpServletResponse();
        var user = User.withUsername("influencer").password("hash").roles("INFLUENCER").build();
        when(jwtService.extrairUsername("token-valido")).thenReturn("influencer");
        when(userDetailsService.loadUserByUsername("influencer")).thenReturn(user);
        when(jwtService.tokenValido("token-valido", user)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("influencer");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_INFLUENCER");
        verify(filterChain).doFilter(request, response);
    }
}
