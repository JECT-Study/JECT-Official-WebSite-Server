package org.ject.support.domain.ministudy.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.ministudy.dto.MiniStudyResponse;
import org.ject.support.domain.ministudy.service.MiniStudyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ministudies")
@RequiredArgsConstructor
public class MiniStudyController implements MiniStudyApiSpec {

    private final MiniStudyService miniStudyService;

    @Override
    @GetMapping
    public Page<MiniStudyResponse> findMiniStudies(@PageableDefault(size = 12) Pageable pageable) {
        return miniStudyService.findMiniStudies(pageable);
    }
}
