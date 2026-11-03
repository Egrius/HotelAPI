package by.Egrius.demoTask.dto.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.List;

@Schema(description = "Расширенная информация об отеле")
public record HotelReadDto(
        @Schema(description = "Уникальный идентификатор отеля", example = "1")
        Long id,

        @Schema(description = "Название отеля", example = "DoubleTree by Hilton Minsk")
        String name,

        @Schema(description = "Описание отеля",
                example = "The DoubleTree by Hilton Hotel Minsk offers 193 luxurious rooms in the Belorussian capital and stunning views of Minsk city from the hotel's 20th floor ...")
        String description,

        @Schema(description = "Бренд отеля", example = "Hilton")
        String brand,

        @Schema(description = "Адрес отеля")
        AddressReadDto address,

        @Schema(description = "Контакты отеля")
        ContactsReadDto contacts,

        @Schema(description = "Время заселения и выселения")
        ArrivalTimeReadDto arrivalTime,

        @Schema(description = "Список удобств (amenities)",
                example = "[\"Free parking\", \"Free WiFi\", \"Non-smoking rooms\", \"Fitness center\"]")
        List<String> amenities
) {

    @Schema(description = "Адрес отеля")
    public record AddressReadDto(
            @Schema(description = "Номер дома", example = "9")
            Integer houseNumber,

            @Schema(description = "Улица", example = "Pobediteley Avenue")
            String street,

            @Schema(description = "Город", example = "Minsk")
            String city,

            @Schema(description = "Страна", example = "Belarus")
            String country,

            @Schema(description = "Почтовый индекс", example = "220004")
            String postCode
    ) {}

    @Schema(description = "Контактные данные")
    public record ContactsReadDto(
            @Schema(description = "Телефон", example = "+375 17 309-80-00")
            String phone,

            @Schema(description = "Email", example = "doubletreeminsk.info@hilton.com")
            String email
    ) {}

    @Schema(description = "Время заезда и выезда")
    public record ArrivalTimeReadDto(
            @Schema(description = "Время заселения", type = "string", example = "14:00")
            @JsonFormat(pattern = "HH:mm")
            LocalTime checkIn,

            @Schema(description = "Время выселения", type = "string", example = "12:00")
            @JsonFormat(pattern = "HH:mm")
            LocalTime checkOut
    ) {}
}