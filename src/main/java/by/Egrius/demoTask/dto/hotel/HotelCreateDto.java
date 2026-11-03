package by.Egrius.demoTask.dto.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalTime;

@Schema(description = "DTO для создания нового отеля")
public record HotelCreateDto(

        @Schema(description = "Название отеля", example = "DoubleTree by Hilton Minsk")
        @NotBlank
        @Size(max = 100)
        String name,

        @Schema(description = "Описание отеля (опционально)",
                example = "The DoubleTree by Hilton Hotel Minsk offers 193 luxurious rooms in the Belorussian capital and stunning views of Minsk city from the hotel's 20th floor ...")
        @Size(max = 1000)
        String description,

        @Schema(description = "Бренд отеля", example = "Hilton")
        @NotBlank
        @Size(max = 100)
        String brand,

        @Schema(description = "Адрес отеля")
        @NotNull
        @Valid
        AddressCreateDto address,

        @Schema(description = "Контактные данные отеля")
        @NotNull
        @Valid
        ContactsCreateDto contacts,

        @Schema(description = "Время заселения и выселения")
        @NotNull
        @Valid
        ArrivalTimeCreateDto arrivalTime
) {

        @Schema(description = "Адрес при создании отеля")
        public record AddressCreateDto(

                @Schema(description = "Номер дома", example = "9")
                @NotNull
                @Positive
                Integer houseNumber,

                @Schema(description = "Улица", example = "Pobediteley Avenue")
                @NotBlank
                @Size(max = 100)
                String street,

                @Schema(description = "Город", example = "Minsk")
                @NotBlank
                @Size(max = 100)
                String city,

                @Schema(description = "Страна", example = "Belarus")
                @NotBlank
                @Size(max = 100)
                String country,

                @Schema(description = "Почтовый индекс", example = "220004")
                @NotBlank
                @Size(max = 20)
                String postCode
        ) {}

        @Schema(description = "Контакты при создании отеля")
        public record ContactsCreateDto(

                @Schema(description = "Контактный телефон", example = "+375 17 309-80-00")
                @NotBlank
                @Size(max = 150)
                String phone,

                @Schema(description = "Электронная почта", example = "doubletreeminsk.info@hilton.com")
                @NotBlank
                @Email
                @Size(max = 150)
                String email
        ) {}

        @Schema(description = "Время заезда и выезда при создании отеля")
        public record ArrivalTimeCreateDto(

                @Schema(description = "Время заселения (формат HH:mm)", type = "string", example = "14:00")
                @NotNull
                @JsonFormat(pattern = "HH:mm")
                LocalTime checkIn,

                @Schema(description = "Время выселения (опционально, формат HH:mm)", type = "string", example = "12:00")
                @JsonFormat(pattern = "HH:mm")
                LocalTime checkOut
        ) {}
}