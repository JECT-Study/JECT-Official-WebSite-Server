package org.ject.support.domain.member.fixture;

import java.util.List;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.entity.Member;

public final class MemberFixture {

    private String name = "김젝트";
    private String email = "member@test.com";
    private String phoneNumber = "01012345678";
    private List<String> interestedDomains = List.of("커머스");
    private Region region = Region.SEOUL;
    private boolean deleted = false;

    private MemberFixture() {
    }

    public static MemberFixture member() {
        return new MemberFixture();
    }

    public MemberFixture name(String name) {
        this.name = name;
        return this;
    }

    public MemberFixture email(String email) {
        this.email = email;
        return this;
    }

    public MemberFixture phoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public MemberFixture interestedDomains(List<String> interestedDomains) {
        this.interestedDomains = interestedDomains;
        return this;
    }

    public MemberFixture region(Region region) {
        this.region = region;
        return this;
    }

    public MemberFixture deleted() {
        this.deleted = true;
        return this;
    }

    public Member build() {
        Member member = Member.create(name, email, phoneNumber, interestedDomains, region);
        if (deleted) {
            member.delete();
        }
        return member;
    }
}
