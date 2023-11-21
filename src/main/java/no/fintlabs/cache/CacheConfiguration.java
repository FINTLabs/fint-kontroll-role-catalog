package no.fintlabs.cache;

import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheManager;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembership;
import no.fintlabs.roleCatalogRole.RoleCatalogRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class CacheConfiguration {

    private final FintCacheManager fintCacheManager;

    public CacheConfiguration(FintCacheManager fintCacheManager) {
        this.fintCacheManager = fintCacheManager;
    }
    @Bean
    FintCache<String , RoleCatalogRole> roleCatalogRoleCache() {
        return createResourceCache(RoleCatalogRole.class);
    }
    @Bean
    FintCache<String , RoleCatalogMembership> roleCatalogMembershipCache() {
        return createResourceCache(RoleCatalogMembership.class);
    }

    private <V> FintCache<String, V> createResourceCache(Class<V> resourceClass) {
        return fintCacheManager.createCache(
                resourceClass.getName().toLowerCase(Locale.ROOT),
                String.class,
                resourceClass
        );
    }
}
