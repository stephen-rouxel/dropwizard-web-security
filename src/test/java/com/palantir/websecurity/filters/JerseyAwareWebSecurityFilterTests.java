/*
 * (c) Copyright 2016-2017 Palantir Technologies Inc. All rights reserved.
 *
 * (c) Copyright 2023 brightSPARK Labs (from commit `c2774cac049bb0007d14790527ea2499670fef83` onwards).
 * All rights reserved.
 */

package com.palantir.websecurity.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.google.common.net.HttpHeaders;
import com.palantir.websecurity.WebSecurityConfiguration;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Tests for {@link JerseyAwareWebSecurityFilter}. */
public final class JerseyAwareWebSecurityFilterTests {

    private static final WebSecurityConfiguration DEFAULT_CONFIG = WebSecurityConfiguration.DEFAULT;

    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final FilterChain chain = mock(FilterChain.class);

    @Test
    public void testInjectInHttpServletRequests() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");

        JerseyAwareWebSecurityFilter filter =
                new JerseyAwareWebSecurityFilter(DEFAULT_CONFIG, "/jersey/root/*");
        request.setPathInfo("/api");

        filter.doFilter(request, response, chain);

        // only testing 1 header, since the WebSecurityHeaderInjector is tested separately
        assertEquals(
                WebSecurityHeaderInjector.DEFAULT_FRAME_OPTIONS,
                response.getHeader(HttpHeaders.X_FRAME_OPTIONS));
    }

    @Test
    public void testNotInjectForJerseyPathWithStar() throws IOException, ServletException {
        JerseyAwareWebSecurityFilter filter =
                new JerseyAwareWebSecurityFilter(DEFAULT_CONFIG, "/api/*");
        assertNotInjecting(filter);
    }

    @Test
    public void testNotInjectForJerseyPathNoStar() throws IOException, ServletException {
        JerseyAwareWebSecurityFilter filter =
                new JerseyAwareWebSecurityFilter(DEFAULT_CONFIG, "/api/");
        assertNotInjecting(filter);
    }

    @Test
    public void testNotInjectForJerseyPathNoSlash() throws IOException, ServletException {
        JerseyAwareWebSecurityFilter filter =
                new JerseyAwareWebSecurityFilter(DEFAULT_CONFIG, "/api");
        assertNotInjecting(filter);
    }

    private void assertNotInjecting(JerseyAwareWebSecurityFilter filter)
            throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/hello");
        // the servlet path is used to check if the request is for Jersey
        request.setServletPath("/api");

        filter.doFilter(request, response, chain);

        assertNull(response.getHeader(HttpHeaders.X_FRAME_OPTIONS));
    }
}
