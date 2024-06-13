# dropwizard-web-security

[![Build Status](https://github.com/brightsparklabs/dropwizard-web-security/actions/workflows/unit_tests.yml/badge.svg)](https://github.com/brightsparklabs/dropwizard-web-security/actions/workflows/unit_tests.yml)
[![Maven](https://img.shields.io/maven-central/v/com.brightsparklabs/dropwizard-web-security)](https://search.maven.org/artifact/com.brightsparklabs/dropwizard-web-security)


**THIS IS A FORK OF THE UNMAINTAINED
[dropwizard-web-security](https://github.com/palantir/dropwizard-web-security).**

A bundle for applying default web security functionality to a dropwizard application. It covers the following areas:

- [Cross-Origin Resource Sharing (CORS)][cors1] [\[2\]][cors2] [\[3\]][cors3]
- Web Application Security Headers ([Content Security Policy][csp], etc.)


## Compatibility

| Bundle Version | Dropwizard Version | Java Version | Notes
| -------------- | ------------------ | ------------ | ---------
| 2.x.y          | 3.x.y              | 17           |
Dropwizard 3.0 [changed core dropwizard
packages](https://www.dropwizard.io/en/stable/manual/upgrade-notes/upgrade-notes-3_0_x.html#dropwizard-package-structure-and-jpms).
Dropwizard 4.0 [transitioned to
Jakarta](https://www.dropwizard.io/en/stable/manual/upgrade-notes/upgrade-notes-4_0_x.html#transition-to-jakarta-ee).
| 1.x.y          | 1.x.y              | 8            | Initial release


## Usage

1. Add the dependency to your project.

    ```groovy
    repository {
        mavenCentral()
    }

    dependencies {
        compile 'com.brightsparklabs:dropwizard-web-security:<latest-version>'
    }
    ```

2. Ensure your configuration implements `WebSecurityConfigurable`.

    ```java
    public static final class ExampleConfiguration extends Configuration implements WebSecurityConfigurable {

        @JsonProperty("webSecurity")
        @NotNull
        @Valid
        private final WebSecurityConfiguration webSecurity = WebSecurityConfiguration.DEFAULT;

        public WebSecurityConfiguration getWebSecurityConfiguration() {
            return this.webSecurity;
        }
    }
    ```

3. Add the bundle to your application.

    ```java
    public class ExampleApplication extends Application<ExampleConfiguration> {

        @Override
        public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
            bootstrap.addBundle(new WebSecurityBundle());
        }
    }
    ```


## Configuration

App Security headers are **added by default**. The following are the default values, **only specify values in your
configuration if they differ from the default values shown below**.

```yaml
webSecurity:
  contentSecurityPolicy: "default-src 'self'; style-src 'self' 'unsafe-inline'; frame-ancestors 'self';"     # CSP
  contentTypeOptions: "nosniff"                                                     # X-Content-Type-Options
  frameOptions: "sameorigin"                                                        # X-Frame-Options
  xssProtection: "1; mode=block"                                                    # X-XSS-Protection
```

**NOTE:** To disable a specific header, set the value to `""`.


## CORS Configuration

CORS is **disabled by default**. To enable CORS, set the `allowedOrigins` method to a non-empty string.

The following are the default values, only specify values if they differ from the default values shown below.

```yaml
webSecurity:
  cors:
    allowCredentials: false
    allowedHeaders: "Accept,Authorization,Content-Type,Origin,X-Requested-With"
    allowedMethods: "DELETE,GET,HEAD,POST,PUT"
    allowedOrigins: ""
    chainPreflight: true
    exposedHeaders: ""
    preflightMaxAge: 1800
```

**NOTE:** The values shown are from [`CrossOriginFilter`][corsfilter], except the following:

- `allowedOrigins` - set to blank instead of `"*"` to require the user to enter the allowed origins
- `allowCredentials` - set to false by default since credentials should be passed via the `Authorization` header
- `allowedHeaders` - set to include the default set of headers and the `Authorization` header
- `allowedMethods` - set to include a default set of commonly used methods


## Advanced Usage

### App-Specific Settings
You can customize your application's defaults by defining it inside of your Dropwizard application. Any value not set
will be set to the default values.

**Note:** the application default values will be **overridden by the YAML defined values**.

```java
public static final class ExampleApplication extends Application<ExampleConfiguration> {

    private final WebSecurityConfiguration webSecurityDefaults = WebSecurityConfiguration.builder()

            // set app defaults for different header values
            .contentSecurityPolicy(CSP_FROM_APP)
            .contentTypeOptions(CTO_FROM_APP)

            // CORS is still DISABLED, since the allowedOrigins is not set, but the default value will be
            // respected if it's ever turned on
            .cors(CorsConfiguration.builder()
                    .preflightMaxAge(60 * 10)
                    .build())

            .build();

    private final WebSecurityBundle webSecurityBundle = new WebSecurityBundle(this.webSecurityDefaults);

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.addBundle(this.webSecurityBundle);
    }
}
```


### Using the Derived Configuration
You can also get the derived configuration to create a matching `WebSecurityHeaderInjector`:

```java
WebSecurityHeaderInjector injector = new WebSecurityHeaderInjector(webSecurityBundle.getDerivedConfiguration());
```


## Contributing

Before working on the code, if you plan to contribute changes, please read the [CONTRIBUTING](CONTRIBUTING.md) document.


## License

Copyright (c) 2023 brightSPARK Labs (from commit `c2774cac049bb0007d14790527ea2499670fef83`
onwards). All rights reserved.

Copyright (c) 2016 Palantir Technologies Inc (to and including commit
`c2774cac049bb0007d14790527ea2499670fef83`). All rights reserved.

This project is made available under the [Apache 2.0 License][license].

[cors1]: https://www.w3.org/TR/cors/
[cors2]: https://www.owasp.org/index.php/CORS_OriginHeaderScrutiny
[cors3]: https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS
[csp]: https://developer.mozilla.org/en-US/docs/Web/Security/CSP

[corsfilter]: https://github.com/eclipse/jetty.project/blob/jetty-9.2.13.v20150730/jetty-servlets/src/main/java/org/eclipse/jetty/servlets/CrossOriginFilter.java

[license]: http://www.apache.org/licenses/LICENSE-2.0
