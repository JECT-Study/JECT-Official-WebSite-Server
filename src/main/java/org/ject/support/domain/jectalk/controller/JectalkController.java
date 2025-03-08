package org.ject.support.domain.jectalk.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.RestPage;
import org.ject.support.domain.jectalk.dto.JectalkResponse;
import org.ject.support.domain.jectalk.service.JectalkService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jectalks")
@RequiredArgsConstructor
public class JectalkController {

    private final JectalkService jectalkService;

    @GetMapping
    public RestPage<JectalkResponse> findJectalks(@PageableDefault(size = 12) Pageable pageable) {
        return jectalkService.findJectalks(pageable);
    }
}
