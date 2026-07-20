package org.ject.support.domain.recruit.domain;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.MemberType;

@Getter
@RequiredArgsConstructor
public enum RecruitType {
    SEMESTER("정규 기수 모집"),
    MAKERS("메이커스 모집"),
    SUPPORTERS("운영 서포터즈 모집"),

    // TODO: 기존의 recruit_type 데이터가 SEMESTER 및 recruitTypeDetail로 마이그레이션될 때까지 기존 값은 유지
    REGULAR("정규 모집"),
    REGULAR_WAITLIST("정규 모집 - 추가합격"),
    BACKFILL("기존 기수 모집"),
    MANUAL("별도 합류"),
    ;

    private final String description;

    public boolean supports(final RecruitTypeDetail recruitTypeDetail) {
        if (recruitTypeDetail == null) {
            return false;
        }
        return switch (this) {
            case SEMESTER -> recruitTypeDetail == RecruitTypeDetail.REGULAR
                    || recruitTypeDetail == RecruitTypeDetail.REFILL;
            case MAKERS, SUPPORTERS -> recruitTypeDetail == RecruitTypeDetail.NEW
                    || recruitTypeDetail == RecruitTypeDetail.REFILL;
            case REGULAR, REGULAR_WAITLIST, BACKFILL, MANUAL -> true;
        };
    }

    public MemberType toMemberType() {
        return switch (this) {
            case MAKERS -> MemberType.MAKERS;
            case SUPPORTERS -> MemberType.SUPPORTERS;
            case SEMESTER, REGULAR, REGULAR_WAITLIST, BACKFILL, MANUAL -> MemberType.SEMESTER;
        };
    }
}
