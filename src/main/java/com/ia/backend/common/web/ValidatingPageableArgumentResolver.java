package com.ia.backend.common.web;

import com.ia.backend.common.exception.BadRequestException;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link PageableHandlerMethodArgumentResolver} that rejects out-of-range pagination
 * requests instead of silently clamping them.
 *
 * <p>The default {@code page}/{@code size} parameters are validated before the request is
 * handed to Spring Data: {@code size} must be a positive integer no greater than the
 * configured maximum and {@code page} must not be negative. Anything else fails with a
 * {@code 400 Bad Request} on the unified error envelope, which keeps a client from
 * requesting an unbounded page ({@code ?size=1000000}) and turning a list endpoint into
 * an accidental full-table scan.</p>
 */
public class ValidatingPageableArgumentResolver extends PageableHandlerMethodArgumentResolver {

    private final int maxPageSize;

    public ValidatingPageableArgumentResolver(int defaultPageSize, int maxPageSize) {
        if (defaultPageSize < 1) {
            throw new IllegalArgumentException("defaultPageSize must be at least 1.");
        }
        if (maxPageSize < 1) {
            throw new IllegalArgumentException("maxPageSize must be at least 1.");
        }
        if (defaultPageSize > maxPageSize) {
            throw new IllegalArgumentException("defaultPageSize must not exceed maxPageSize.");
        }

        this.maxPageSize = maxPageSize;
        setFallbackPageable(PageRequest.of(0, defaultPageSize));
        setMaxPageSize(maxPageSize);
    }

    @Override
    public Pageable resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        validatePage(parameter, webRequest);
        validateSize(parameter, webRequest);

        return super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
    }

    private void validatePage(MethodParameter parameter, NativeWebRequest webRequest) {
        String name = getParameterNameToUse(getPageParameterName(), parameter);
        String raw = webRequest.getParameter(name);
        if (raw == null || raw.isBlank()) {
            return;
        }

        int page = parse(name, raw);
        int minimum = isOneIndexedParameters() ? 1 : 0;
        if (page < minimum) {
            throw new BadRequestException(
                    "Pagination parameter '" + name + "' must be " + minimum + " or greater.");
        }
    }

    private void validateSize(MethodParameter parameter, NativeWebRequest webRequest) {
        String name = getParameterNameToUse(getSizeParameterName(), parameter);
        String raw = webRequest.getParameter(name);
        if (raw == null || raw.isBlank()) {
            return;
        }

        int size = parse(name, raw);
        if (size < 1 || size > maxPageSize) {
            throw new BadRequestException(
                    "Pagination parameter '" + name + "' must be between 1 and " + maxPageSize + ".");
        }
    }

    private int parse(String name, String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Pagination parameter '" + name + "' must be a valid integer.");
        }
    }
}
