package by.Egrius.demoTask.dto.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;
import java.util.List;

public record HotelReadDto(
        Long id,
        String name,
        String description,
        String brand,
        AddressReadDto address,
        ContactsReadDto contacts,
        ArrivalTimeReadDto arrivalTime,
        List<String> amenities
) {

    public record AddressReadDto(
            Integer houseNumber,
            String street,
            String city,
            String country,
            String postCode
    ) {}

    public record ContactsReadDto(
            String phone,
            String email
    ) {}

    public record ArrivalTimeReadDto(
            @JsonFormat(pattern = "HH:mm")
            LocalTime checkIn,

            @JsonFormat(pattern = "HH:mm")
            LocalTime checkOut
    ) {}
}