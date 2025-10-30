package org.ject.support.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.ject.support.domain.apply.exception.ApplyErrorCode.NOT_FOUND_APPLY;
import static org.ject.support.domain.apply.exception.ApplyErrorCode.NOT_SUBMITTED;

@Service
@RequiredArgsConstructor
public class ApplyPassService {

    private final ApplyRepository applyRepository;

    @Transactional
    public int passApply(List<Long> applyIds) {
        // 지원 정보 조회
        List<Apply> applies = applyIds.stream()
                .map(id -> applyRepository.findById(id)
                        .orElseThrow(() -> new ApplyException(NOT_FOUND_APPLY)))
                .toList();

        // 지원자 합격 처리
        applies.forEach(apply -> {
            // 지원서 제출 완료 여부 검증
            if (apply.isNotSubmitted()) {
                throw new ApplyException(NOT_SUBMITTED);
            }

            // 지원자 role 승격
            Member member = apply.getMember();
            member.promoteToSemester();
        });

        // 승인한 구성원 수 반환
        return applies.size();
    }
}
