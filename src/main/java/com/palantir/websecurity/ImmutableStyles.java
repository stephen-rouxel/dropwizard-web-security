/*
 * (c) Copyright 2016-2017 Palantir Technologies Inc. All rights reserved.
 *
 * (c) Copyright 2023 brightSPARK Labs (from commit `c2774cac049bb0007d14790527ea2499670fef83` onwards).
 * All rights reserved.
 */

package com.palantir.websecurity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.immutables.value.Value.Style;

/** Styles for immutable classes. */
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Style(visibility = Style.ImplementationVisibility.PACKAGE)
@interface ImmutableStyles {}
