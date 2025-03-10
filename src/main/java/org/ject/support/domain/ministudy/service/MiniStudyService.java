package org.ject.support.domain.ministudy.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.RestPage;
import org.ject.support.domain.ministudy.dto.MiniStudyResponse;
import org.ject.support.domain.ministudy.repository.MiniStudyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MiniStudyService {

    private final MiniStudyRepository miniStudyRepository;

    @Cacheable(value = "ministudy", key = "#pageable.pageNumber")
    @Transactional(readOnly = true)
    public RestPage<MiniStudyResponse> findMiniStudies(Pageable pageable) {
        return miniStudyRepository.findMiniStudies(pageable);
    }
}
