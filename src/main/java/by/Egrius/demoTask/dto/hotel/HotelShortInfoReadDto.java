package by.Egrius.demoTask.dto.hotel;

public record HotelShortInfoReadDto (
        Long id,
        String name,
        String description,
        String address,
        String phone
) { }
