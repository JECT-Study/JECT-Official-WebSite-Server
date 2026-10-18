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
        };
    }

    public MemberType toMemberType() {
        return switch (this) {
            case MAKERS -> MemberType.MAKERS;
            case SUPPORTERS -> MemberType.SUPPORTERS;
            case SEMESTER -> MemberType.SEMESTER;
        };
    }
}
