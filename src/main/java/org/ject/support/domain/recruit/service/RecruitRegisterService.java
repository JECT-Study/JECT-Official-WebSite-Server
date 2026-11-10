package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.dto.SemesterRegistered;
import org.ject.support.domain.recruit.dto.RecruitRegisterRequest;
import org.ject.support.domain.recruit.exception.SemesterErrorCode;
import org.ject.support.domain.recruit.exception.SemesterException;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruitRegisterService {
    private final RecruitRepository recruitRepository;
    private final SemesterRepository semesterRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleRegisterRecruitEvent(SemesterRegistered event) throws DataIntegrityViolationException {
        Long semesterId = event.semesterId();
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new SemesterException(SemesterErrorCode.NOT_FOUND_SEMESTER));
        if (event.recruitRegisterRequests().stream().anyMatch(request ->
                request.recruitType() != RecruitType.SEMESTER || request.semesterId() != null)) {
            throw new RecruitException(RecruitErrorCode.INVALID_RECRUIT_SEMESTER);
        }
        List<Recruit> recruits = event.recruitRegisterRequests().stream()
                .map(request -> request.toEntity(semester)).toList();
        createRecruits(recruits);
    }

    // 공고별로 지정한 기수를 조회하여 모집 유형에 맞게 등록
    @Transactional
    public void registerRecruits(List<RecruitRegisterRequest> requests) {
        List<Recruit> recruits = requests.stream().map(request -> {
            Semester semester = request.semesterId() == null ? null
                    : semesterRepository.findById(request.semesterId())
                    .orElseThrow(() -> new SemesterException(SemesterErrorCode.NOT_FOUND_SEMESTER));
            return request.toEntity(semester);
        }).toList();
        createRecruits(recruits);
    }

    // 요청 내부와 기존 미종료 공고의 중복을 검증한 뒤 일괄 저장
    private void createRecruits(List<Recruit> recruits) {
        var keys = new HashSet<List<Object>>();
        for (Recruit recruit : recruits) {
            Long semesterId = recruit.getSemester() == null ? null : recruit.getSemester().getId();
            if (!keys.add(Arrays.asList(recruit.getRecruitType(), recruit.getJobFamily(), semesterId))
                    || recruitRepository.existsDuplicateRecruit(recruit.getRecruitType(), recruit.getJobFamily(),
                    semesterId, null)) {
                throw new RecruitException(RecruitErrorCode.DUPLICATED_JOB_FAMILY);
            }
        }
        recruitRepository.saveAll(recruits);
    }
}
