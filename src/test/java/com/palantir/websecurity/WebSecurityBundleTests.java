/*
 * (c) Copyright 2016-2017 Palantir Technologies Inc. All rights reserved.
 *
 * (c) Copyright 2023 brightSPARK Labs (from commit `c2774cac049bb0007d14790527ea2499670fef83` onwards).
 * All rights reserved.
 */

package com.palantir.websecurity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dropwizard.core.setup.Environment;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import java.util.Map;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Tests for {@link WebSecurityBundle}. */
public final class WebSecurityBundleTests {

    private final WebSecurityConfigurable appConfig = mock(WebSecurityConfigurable.class);
    private final FilterRegistration.Dynamic dynamic = mock(FilterRegistration.Dynamic.class);
    private final Environment environment = mock(Environment.class, RETURNS_DEEP_STUBS);

    @Test
    public void testDefaultFiltersApplied() throws Exception {
        WebSecurityBundle bundle = new WebSecurityBundle();
        WebSecurityConfiguration webSecurityConfig = WebSecurityConfiguration.DEFAULT;

        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(webSecurityConfig);

        bundle.run(this.appConfig, this.environment);

        verify(this.environment.servlets(), never())
                .addFilter(anyString(), isA(CrossOriginFilter.class));
    }

    @Test
    public void testFiltersAppliedWhenEnabled() throws Exception {
        WebSecurityBundle bundle = new WebSecurityBundle();
        WebSecurityConfiguration webSecurityConfig =
                WebSecurityConfiguration.builder()
                        .cors(CorsConfiguration.builder().allowedOrigins("http://origin").build())
                        .build();

        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(webSecurityConfig);

        bundle.run(this.appConfig, this.environment);

        verify(this.environment.servlets()).addFilter(anyString(), isA(CrossOriginFilter.class));
    }

    @Test
    public void testFiltersNotAppliedWhenDisabled() throws Exception {
        WebSecurityBundle bundle = new WebSecurityBundle();
        WebSecurityConfiguration webSecurityConfig =
                WebSecurityConfiguration.builder().cors(CorsConfiguration.DISABLED).build();

        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(webSecurityConfig);

        bundle.run(this.appConfig, this.environment);

        verify(this.environment.servlets(), never())
                .addFilter(anyString(), isA(CrossOriginFilter.class));
    }

    @Test
    public void testYamlOverridesAppDefaults() throws Exception {
        WebSecurityConfiguration appDefaultConfig =
                WebSecurityConfiguration.builder()
                        .cors(CorsConfiguration.builder().allowedOrigins("http://origin").build())
                        .build();
        WebSecurityConfiguration yamlConfig =
                WebSecurityConfiguration.builder().cors(CorsConfiguration.DISABLED).build();
        WebSecurityBundle bundle = new WebSecurityBundle(appDefaultConfig);

        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(yamlConfig);

        bundle.run(this.appConfig, this.environment);

        verify(this.environment.servlets(), never())
                .addFilter(anyString(), isA(CrossOriginFilter.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDefaultPropertyMap() throws Exception {
        WebSecurityConfiguration appDefaultConfig =
                WebSecurityConfiguration.builder()
                        .cors(CorsConfiguration.builder().allowedOrigins("http://origin").build())
                        .build();
        WebSecurityBundle bundle = new WebSecurityBundle(appDefaultConfig);

        when(environment.servlets().addFilter(anyString(), any(Filter.class))).thenReturn(dynamic);
        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(appDefaultConfig);

        bundle.run(appConfig, environment);

        ArgumentCaptor<Map> paramCaptor = ArgumentCaptor.forClass(Map.class);
        verify(dynamic).setInitParameters(paramCaptor.capture());

        Map<String, String> captured = paramCaptor.getValue();
        assertEquals(
                Boolean.toString(WebSecurityBundle.DEFAULT_ALLOW_CREDENTIALS),
                captured.get(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM));
        assertEquals(
                WebSecurityBundle.DEFAULT_ALLOWED_HEADERS,
                captured.get(CrossOriginFilter.ALLOWED_HEADERS_PARAM));
        assertEquals(
                WebSecurityBundle.DEFAULT_ALLOWED_METHODS,
                captured.get(CrossOriginFilter.ALLOWED_METHODS_PARAM));

        assertNull(captured.get(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM));
        assertNull(captured.get(CrossOriginFilter.EXPOSED_HEADERS_PARAM));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPropertyMapAddsAll() throws Exception {
        CorsConfiguration config =
                CorsConfiguration.builder()
                        .allowedOrigins("origins")
                        .allowedMethods("methods")
                        .allowedHeaders("headers")
                        .chainPreflight(false)
                        .preflightMaxAge(123)
                        .allowCredentials(true)
                        .exposedHeaders("exposed")
                        .build();
        WebSecurityConfiguration appDefaultConfig =
                WebSecurityConfiguration.builder().cors(config).build();
        WebSecurityBundle bundle = new WebSecurityBundle(appDefaultConfig);

        when(environment.servlets().addFilter(anyString(), any(Filter.class))).thenReturn(dynamic);
        when(this.appConfig.getWebSecurityConfiguration()).thenReturn(appDefaultConfig);

        bundle.run(appConfig, environment);

        ArgumentCaptor<Map> paramCaptor = ArgumentCaptor.forClass(Map.class);
        verify(dynamic).setInitParameters(paramCaptor.capture());

        Map<String, String> props = paramCaptor.getValue();
        assertEquals(
                config.allowedOrigins().get(), props.get(CrossOriginFilter.ALLOWED_ORIGINS_PARAM));
        assertEquals(
                config.allowedMethods().get(), props.get(CrossOriginFilter.ALLOWED_METHODS_PARAM));
        assertEquals(
                config.allowedHeaders().get(), props.get(CrossOriginFilter.ALLOWED_HEADERS_PARAM));
        assertEquals(
                config.chainPreflight().get().toString(),
                props.get(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM));
        assertEquals(
                config.preflightMaxAge().get().toString(),
                props.get(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM));
        assertEquals(
                config.allowCredentials().get().toString(),
                props.get(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM));
        assertEquals(
                config.exposedHeaders().get(), props.get(CrossOriginFilter.EXPOSED_HEADERS_PARAM));
    }
}
