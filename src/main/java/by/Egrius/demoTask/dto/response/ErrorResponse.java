package by.Egrius.demoTask.dto.response;

import java.util.List;

public record ErrorResponse(
        String message, List<ErrorDto> errors
) {
    public record ErrorDto (
            String field, String message
    ) {}
}