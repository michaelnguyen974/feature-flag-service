package com.example.open_feature_demo2_real;

import dev.openfeature.contrib.providers.flagd.FlagdProvider;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import dev.openfeature.sdk.OpenFeatureAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenFeatureBeans {
    private final FlagdProvider provider = new FlagdProvider();

    @Bean
    public OpenFeatureAPI OpenFeatureAPI() {
        final OpenFeatureAPI openFeatureAPI = OpenFeatureAPI.getInstance();

        // Use flagd as the OpenFeature provider and use default configurations
        try {
            openFeatureAPI.setProviderAndWait(provider);
        } catch (OpenFeatureError e) {
            throw new RuntimeException("Failed to set OpenFeature provider", e);
        }

        return openFeatureAPI;
    }
}
