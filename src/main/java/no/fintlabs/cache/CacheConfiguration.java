package no.fintlabs.cache;

import no.fintlabs.member.Member;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembership;
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
    FintCache<String, Integer> roleCatalogRoleCache() {
        return createCache(Integer.class, String.class);
    }

    @Bean
    FintCache<String, RoleCatalogMembership> roleCatalogMembershipCache() {
        return createCache(RoleCatalogMembership.class, String.class);
    }

    @Bean
    FintCache<Long, Member> memberCache() {
        return createCache(Member.class, Long.class);
    }

    private <K, V> FintCache<K, V> createCache(Class<V> resourceClass, Class<K> keyClass) {
        return fintCacheManager.createCache(
                resourceClass.getName().toLowerCase(Locale.ROOT),
                keyClass,
                resourceClass
        );
    }
}
