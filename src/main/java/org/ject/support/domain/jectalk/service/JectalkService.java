package org.ject.support.domain.jectalk.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.RestPage;
import org.ject.support.domain.jectalk.dto.JectalkResponse;
import org.ject.support.domain.jectalk.repository.JectalkRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JectalkService {

    private final JectalkRepository jectalkRepository;

    @Cacheable(value = "jectalk", key = "#pageable.pageNumber")
    @Transactional(readOnly = true)
    public RestPage<JectalkResponse> findJectalks(Pageable pageable) {
        return jectalkRepository.findJectalks(pageable);
    }
}
