package org.ject.support.admin.mail.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.dto.MailRecruitResponse;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailRecruitService {

    private final RecruitRepository recruitRepository;

    public List<MailRecruitResponse> getRecruits() {
        return recruitRepository.findAllForMailDispatch().stream()
                .map(MailRecruitResponse::from)
                .toList();
    }
}
