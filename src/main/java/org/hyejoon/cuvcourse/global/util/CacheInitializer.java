package org.hyejoon.cuvcourse.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.hyejoon.cuvcourse.domain.lecture.cache.LectureCacheService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheInitializer implements CommandLineRunner {

    private final CacheManager cacheManager;

    @Override
    public void run(String... args) throws Exception {
        log.info("Clearing 'lectures' cache on startup...");
        Optional.of(cacheManager.getCache(LectureCacheService.LECTURE_CACHE_VALUE)).ifPresent(c -> c
            .clear());
        log.info("'lectures' cache cleared.");
    }
}
