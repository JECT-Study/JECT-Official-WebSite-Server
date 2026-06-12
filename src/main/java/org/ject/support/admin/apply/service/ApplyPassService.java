package org.ject.support.admin.apply.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.ject.support.domain.apply.exception.ApplyErrorCode.NOT_SUBMITTED;

@Service
@RequiredArgsConstructor
public class ApplyPassService {

    private final ApplyRepository applyRepository;

    @Transactional
    public int passApply(List<Long> applyIds) {
        // 지원 정보 조회
        List<Apply> applies = applyRepository.findAllByIdWithApplicant(applyIds);

        // 지원자 합격 처리
        applies.forEach(apply -> {
            // 지원서 제출 완료 여부 검증
            if (apply.isNotSubmitted()) {
                throw new ApplyException(NOT_SUBMITTED);
            }

            // 지원자 role 승격
            Applicant applicant = apply.getApplicant();
            applicant.promoteToSemester();
            applicant.updateMemberType(apply.getRecruit().getRecruitType().toMemberType());
        });

        // 승인한 구성원 수 반환
        return applies.size();
    }
}
