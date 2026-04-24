package com.casavo.leadrouting.leadrouting.adapter.in.rest.dto;

import com.casavo.leadrouting.leadrouting.application.query.AssignmentPage;

import java.util.List;
import java.util.UUID;

public record AssignmentPageResponse(List<AssignmentItemResponse> data, PageInfo pageInfo) {

    public record AssignmentItemResponse(UUID assignmentId, UUID leadId, UUID agentId, java.time.Instant assignedAt) {}

    public record PageInfo(String nextCursor, boolean hasMore, int limit) {}

    public static AssignmentPageResponse from(AssignmentPage page) {
        List<AssignmentItemResponse> items = page.data().stream()
                .map(a -> new AssignmentItemResponse(
                        a.id().value(),
                        a.leadId().value(),
                        a.agentId().value(),
                        a.assignedAt()))
                .toList();

        String nextCursor = page.nextCursor().map(c -> c.encode()).orElse(null);
        return new AssignmentPageResponse(items, new PageInfo(nextCursor, page.hasMore(), page.limit()));
    }
}
