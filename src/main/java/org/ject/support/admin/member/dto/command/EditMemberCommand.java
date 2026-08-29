package org.ject.support.admin.member.dto.command;

import java.util.List;

import org.ject.support.admin.member.dto.request.UpdateMemberMakersRequest;
import org.ject.support.admin.member.dto.request.UpdateMemberSupportersRequest;
import org.ject.support.domain.member.Region;

public record EditMemberCommand(
	String name,
	String email,
	String phoneNumber,
	Region region,
	List<String> interestedDomains
) {
	public static EditMemberCommand from(UpdateMemberMakersRequest request) {
		return new EditMemberCommand(
			request.name(),
			request.email(),
			request.phoneNumber(),
			request.region(),
			request.interestedDomains()
		);
	}

	public static EditMemberCommand from(UpdateMemberSupportersRequest request) {
		return new EditMemberCommand(
			request.name(),
			request.email(),
			request.phoneNumber(),
			null,
			null
		);
	}
}
