package by.Egrius.demoTask.dto.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalTime;

public record HotelCreateDto(

        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 1000)
        String description,

        @NotBlank
        @Size(max = 100)
        String brand,

        @NotNull
        @Valid
        AddressCreateDto address,

        @NotNull
        @Valid
        ContactsCreateDto contacts,

        @NotNull
        @Valid
        ArrivalTimeCreateDto arrivalTime
) {

    public record AddressCreateDto(

            @NotNull
            @Positive
            Integer houseNumber,

            @NotBlank
            @Size(max = 100)
            String street,

            @NotBlank
            @Size(max = 100)
            String city,

            @NotBlank
            @Size(max = 100)
            String country,

            @NotBlank
            @Size(max = 20)
            String postCode
    ) {}

    public record ContactsCreateDto(

            @NotBlank
            @Size(max = 150)
            String phone,

            @NotBlank
            @Email
            @Size(max = 150)
            String email
    ) {}

    public record ArrivalTimeCreateDto(

            @NotNull
            LocalTime checkIn,

            LocalTime checkOut
    ) {}
}