package com.casavo.leadrouting.leadrouting.application.query;

import com.casavo.leadrouting.leadrouting.domain.model.Assignment;

import java.util.List;
import java.util.Optional;

public record AssignmentPage(
        List<Assignment> data,
        Optional<Cursor> nextCursor,
        boolean hasMore,
        int limit
) {}
