package org.ject.support.admin.member.dto.command;

import org.ject.support.admin.member.dto.request.EditMemberSemesterRequest;

public record EditMemberSemesterCommand(
	Long semesterId,
	Long teamId,
	String certNumber,
	String firstReview,
	String secondReview
) {
	public static EditMemberSemesterCommand from(EditMemberSemesterRequest request) {
		return new EditMemberSemesterCommand(
			request.semesterId(),
			request.teamId(),
			request.certNumber(),
			request.firstReview(),
			request.secondReview()
		);
	}
}
