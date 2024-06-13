/*
 * (c) Copyright 2016-2017 Palantir Technologies Inc. All rights reserved.
 *
 * (c) Copyright 2023 brightSPARK Labs (from commit `c2774cac049bb0007d14790527ea2499670fef83` onwards).
 * All rights reserved.
 */

package com.palantir.websecurity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Used by the application's {@link io.dropwizard.core.Configuration} to provide a {@link
 * WebSecurityConfiguration}.
 */
public interface WebSecurityConfigurable {

    @NotNull
    @Valid
    WebSecurityConfiguration getWebSecurityConfiguration();
}
