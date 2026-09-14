package org.ject.support.admin.member.dto.command;

import java.time.LocalDate;

import org.ject.support.admin.member.dto.request.UpdateMemberSupportersRequest;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record EditMemberSupportersActivityCommand(
	JobFamily jobFamily,
	RecruitTypeDetail recruitTypeDetail,
	LocalDate startDate,
	LocalDate endDate,
	String memo
) {
	public static EditMemberSupportersActivityCommand from(UpdateMemberSupportersRequest request) {
		return new EditMemberSupportersActivityCommand(
			request.jobFamily(),
			request.recruitTypeDetail(),
			request.startDate(),
			request.endDate(),
			request.memo()
		);
	}
}
