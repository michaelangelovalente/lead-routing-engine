package com.casavo.leadrouting.common;

import java.net.URI;

public final class ProblemTypes {

    private static final String BASE = "https://lead-engine.com/problems/";

    public static final URI VALIDATION_FAILURE = URI.create(BASE + "validation-failure");
    public static final URI INVALID_CURSOR     = URI.create(BASE + "invalid-cursor");
    public static final URI NO_ELIGIBLE_AGENT  = URI.create(BASE + "no-eligible-agent");
    public static final URI AGENT_NOT_FOUND    = URI.create(BASE + "agent-not-found");

    private ProblemTypes() {}
}
