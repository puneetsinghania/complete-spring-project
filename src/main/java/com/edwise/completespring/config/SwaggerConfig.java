package com.edwise.completespring.config;

import com.fasterxml.classmate.TypeResolver;
import com.mangofactory.swagger.configuration.JacksonScalaSupport;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.configuration.SwaggerGlobalSettings;
import com.mangofactory.swagger.core.DefaultSwaggerPathProvider;
import com.mangofactory.swagger.core.SwaggerApiResourceListing;
import com.mangofactory.swagger.core.SwaggerPathProvider;
import com.mangofactory.swagger.models.alternates.AlternateTypeProvider;
import com.mangofactory.swagger.models.alternates.WildcardType;
import com.mangofactory.swagger.scanners.ApiListingReferenceScanner;
import com.wordnik.swagger.model.*;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.mangofactory.swagger.models.alternates.Alternates.newRule;

/**
 * Created by user EAnton on 04/04/2014.
 */
@Configuration
@ComponentScan(basePackages = "com.mangofactory.swagger.configuration")
public class SwaggerConfig {

    public static final List<String> DEFAULT_INCLUDE_PATTERNS = Arrays.asList("/.*");
    public static final String SWAGGER_GROUP = "external";
    public static final String RELATIVE_GROUP = "";

    @Value("${app.swagger.docs}")
    private String docsLocation;

    @Autowired
    private SpringSwaggerConfig springSwaggerConfig;

    /**
     * Adds the jackson scala module to the MappingJackson2HttpMessageConverter registered with spring
     * Swagger core models are scala so we need to be able to convert to JSON
     * Also registers some custom serializers needed to transform swagger models to swagger-ui required json format
     */
    @Bean
    public JacksonScalaSupport jacksonScalaSupport() {
        JacksonScalaSupport jacksonScalaSupport = new JacksonScalaSupport();
        //Set to false to disable
        jacksonScalaSupport.setRegisterScalaModule(true);
        return jacksonScalaSupport;
    }


    /**
     * Global swagger settings
     */
    @Bean
    public SwaggerGlobalSettings swaggerGlobalSettings() {
        SwaggerGlobalSettings swaggerGlobalSettings = new SwaggerGlobalSettings();
        swaggerGlobalSettings.setGlobalResponseMessages(springSwaggerConfig.defaultResponseMessages());

        Set<Class> ignorableParameterTypes = springSwaggerConfig.defaultIgnorableParameterTypes();
        ignorableParameterTypes.add(LocalDate.class);
        swaggerGlobalSettings.setIgnorableParameterTypes(ignorableParameterTypes);

        AlternateTypeProvider alternateTypeProvider = springSwaggerConfig.defaultAlternateTypeProvider();
        TypeResolver typeResolver = new TypeResolver();
        alternateTypeProvider.addRule(newRule(typeResolver.resolve(ResponseEntity.class),
                typeResolver.resolve(Void.class)));
        alternateTypeProvider.addRule(newRule(typeResolver.resolve(ResponseEntity.class, WildcardType.class),
                typeResolver.resolve(WildcardType.class)));
        alternateTypeProvider.addRule(newRule(typeResolver.resolve(HttpEntity.class, WildcardType.class),
                typeResolver.resolve(WildcardType.class)));
        swaggerGlobalSettings.setAlternateTypeProvider(alternateTypeProvider);

        return swaggerGlobalSettings;
    }

    /**
     * API Info as it appears on the swagger-ui page
     */
    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "Books API",
                "Your book database!",
                "http://en.wikipedia.org/wiki/Terms_of_service",
                "edwise.null@gmail.com",
                "Apache 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0.html"
        );
        return apiInfo;
    }

    /**
     * Configure a SwaggerApiResourceListing for each swagger instance within your app. e.g. 1. private  2. external apis
     * Required to be a spring bean as spring will call the postConstruct method to bootstrap swagger scanning.
     *
     * @return SwaggerApiResourceListing
     */
    @Bean
    public SwaggerApiResourceListing swaggerApiResourceListing() {
        //The group name is important and should match the group set on ApiListingReferenceScanner
        //Note that swaggerCache() is by DefaultSwaggerController to serve the swagger json
        SwaggerApiResourceListing swaggerApiResourceListing = new SwaggerApiResourceListing(springSwaggerConfig.swaggerCache(), SWAGGER_GROUP);

        //Set the required swagger settings
        swaggerApiResourceListing.setSwaggerGlobalSettings(swaggerGlobalSettings());

        //Use a custom path provider or springSwaggerConfig.defaultSwaggerPathProvider()
        swaggerApiResourceListing.setSwaggerPathProvider(apiPathProvider());

        //Supply the API Info as it should appear on swagger-ui web page
        swaggerApiResourceListing.setApiInfo(apiInfo());

        //Global authorization - see the swagger documentation
        swaggerApiResourceListing.setAuthorizationTypes(authorizationTypes());

        //Every SwaggerApiResourceListing needs an ApiListingReferenceScanner to scan the spring request mappings
        swaggerApiResourceListing.setApiListingReferenceScanner(apiListingReferenceScanner());
        return swaggerApiResourceListing;
    }

    @Bean
    /**
     * The ApiListingReferenceScanner does most of the work.
     * Scans the appropriate spring RequestMappingHandlerMappings
     * Applies the correct absolute paths to the generated swagger resources
     */
    public ApiListingReferenceScanner apiListingReferenceScanner() {
        ApiListingReferenceScanner apiListingReferenceScanner = new ApiListingReferenceScanner();

        //Picks up all of the registered spring RequestMappingHandlerMappings for scanning
        apiListingReferenceScanner.setRequestMappingHandlerMapping(springSwaggerConfig.swaggerRequestMappingHandlerMappings());

        //Excludes any controllers with the supplied annotations
        apiListingReferenceScanner.setExcludeAnnotations(springSwaggerConfig.defaultExcludeAnnotations());

        //
        apiListingReferenceScanner.setResourceGroupingStrategy(springSwaggerConfig.defaultResourceGroupingStrategy());

        //Path provider used to generate the appropriate uri's
        apiListingReferenceScanner.setSwaggerPathProvider(apiPathProvider());

        //Must match the swagger group set on the SwaggerApiResourceListing
        apiListingReferenceScanner.setSwaggerGroup(SWAGGER_GROUP);

        //Only include paths that match the supplied regular expressions
        apiListingReferenceScanner.setIncludePatterns(DEFAULT_INCLUDE_PATTERNS);

        return apiListingReferenceScanner;
    }

    /**
     * Example of a custom path provider
     */
    @Bean
    public ApiPathProvider apiPathProvider() {
        ApiPathProvider apiPathProvider = new ApiPathProvider(docsLocation);
        apiPathProvider.setDefaultSwaggerPathProvider(springSwaggerConfig.defaultSwaggerPathProvider());
        return apiPathProvider;
    }


    private List<AuthorizationType> authorizationTypes() {
        ArrayList<AuthorizationType> authorizationTypes = new ArrayList<>();

        List<AuthorizationScope> authorizationScopeList = newArrayList();
        authorizationScopeList.add(new AuthorizationScope("global", "access all"));

        List<GrantType> grantTypes = newArrayList();

        LoginEndpoint loginEndpoint = new LoginEndpoint(apiPathProvider().getAppBasePath() + "/user/authenticate");
        grantTypes.add(new ImplicitGrant(loginEndpoint, "access_token"));

        return authorizationTypes;
    }

    @Bean
    public ApiListingReferenceScanner relativeApiListingReferenceScanner() {
        ApiListingReferenceScanner apiListingReferenceScanner = new ApiListingReferenceScanner();
        apiListingReferenceScanner.setRequestMappingHandlerMapping(springSwaggerConfig.swaggerRequestMappingHandlerMappings());
        apiListingReferenceScanner.setExcludeAnnotations(springSwaggerConfig.defaultExcludeAnnotations());
        apiListingReferenceScanner.setResourceGroupingStrategy(springSwaggerConfig.defaultResourceGroupingStrategy());
        apiListingReferenceScanner.setSwaggerPathProvider(relativeSwaggerPathProvider());
        apiListingReferenceScanner.setSwaggerGroup(RELATIVE_GROUP);
        apiListingReferenceScanner.setIncludePatterns(DEFAULT_INCLUDE_PATTERNS);
        return apiListingReferenceScanner;
    }

    @Bean
    public SwaggerPathProvider relativeSwaggerPathProvider() {
        return new ApiRelativeSwaggerPathProvider();
    }

    private class ApiRelativeSwaggerPathProvider extends DefaultSwaggerPathProvider {
        @Override
        public String getAppBasePath() {
            return "/";
        }
    }
}