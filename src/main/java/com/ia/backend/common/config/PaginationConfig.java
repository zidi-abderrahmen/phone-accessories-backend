package com.ia.backend.common.config;

import com.ia.backend.common.web.ValidatingPageableArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Swaps Spring Data's permissive {@code Pageable} resolver for
 * {@link ValidatingPageableArgumentResolver}, so every list endpoint shares the same
 * default page size and page-size cap defined by {@code spring.data.web.pageable.*}.
 */
@Configuration(proxyBeanMethods = false)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PaginationConfig implements WebMvcConfigurer {

    private final int defaultPageSize;
    private final int maxPageSize;

    public PaginationConfig(
            @Value("${spring.data.web.pageable.default-page-size:20}") int defaultPageSize,
            @Value("${spring.data.web.pageable.max-page-size:100}") int maxPageSize) {
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ValidatingPageableArgumentResolver(defaultPageSize, maxPageSize));
    }
}
