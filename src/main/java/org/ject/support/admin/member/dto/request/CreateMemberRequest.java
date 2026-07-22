package org.ject.support.admin.member.dto.request;

import java.util.List;

import org.ject.support.domain.member.Region;

public interface CreateMemberRequest {

	String name();

	String email();

	String phoneNumber();

	List<String> interestedDomains();

	Region region();
}
