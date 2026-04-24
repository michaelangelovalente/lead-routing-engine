package com.casavo.leadrouting.leadrouting.application.query;

import com.casavo.leadrouting.common.InvalidCursorException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

public record Cursor(Instant assignedAt, UUID id) {

    public Cursor {
        Objects.requireNonNull(assignedAt);
        Objects.requireNonNull(id);
    }

    public String encode() {
        String json = String.format(
                "{\"at\":\"%s\",\"id\":\"%s\",\"v\":1}",
                assignedAt.toString(), id.toString());
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public static Cursor decode(String encoded) {
        try {
            String json = new String(
                    Base64.getUrlDecoder().decode(encoded),
                    StandardCharsets.UTF_8);
            String at = extractJsonString(json, "at");
            String idStr = extractJsonString(json, "id");
            return new Cursor(Instant.parse(at), UUID.fromString(idStr));
        } catch (Exception e) {
            throw new InvalidCursorException("Cursor is malformed or unsupported", e);
        }
    }

    private static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) throw new IllegalArgumentException("Missing key: " + key);
        start += search.length();
        int end = json.indexOf('"', start);
        if (end < 0) throw new IllegalArgumentException("Unterminated value for key: " + key);
        return json.substring(start, end);
    }
}
