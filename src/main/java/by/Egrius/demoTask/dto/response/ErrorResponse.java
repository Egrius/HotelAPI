package by.Egrius.demoTask.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Стандартный ответ с ошибкой")
public record ErrorResponse(
        @Schema(description = "Общее сообщение", example = "Validation failed")
        String message,

        @Schema(description = "Список ошибок по полям")
        List<ErrorDto> errors
) {
    @Schema(description = "Ошибка валидации поля")
    public record ErrorDto(
            @Schema(description = "Имя поля", example = "name")
            String field,
            @Schema(description = "Сообщение", example = "must not be blank")
            String message
    ) {}
}