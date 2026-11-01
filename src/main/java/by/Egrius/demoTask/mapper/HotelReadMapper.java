package by.Egrius.demoTask.mapper;

import by.Egrius.demoTask.dto.hotel.HotelReadDto;
import by.Egrius.demoTask.dto.hotel.HotelShortInfoReadDto;
import by.Egrius.demoTask.entity.Hotel;
import by.Egrius.demoTask.entity.embeddable.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface HotelReadMapper {

    @Mapping(target = "address", source = "address", qualifiedByName = "shortenAddress")
    @Mapping(target = "phone", source = "contacts.phone")
    HotelShortInfoReadDto mapToShortInfoReadDto(Hotel hotel);

    @Mapping(target = "address.houseNumber", source = "address.houseNumber")
    @Mapping(target = "address.street",      source = "address.street")
    @Mapping(target = "address.city",        source = "address.city")
    @Mapping(target = "address.country",     source = "address.country")
    @Mapping(target = "address.postCode",    source = "address.postCode")

    @Mapping(target = "contacts.phone",      source = "contacts.phone")
    @Mapping(target = "contacts.email",      source = "contacts.email")

    @Mapping(target = "arrivalTime.checkIn", source = "arrivalTime.checkIn")
    @Mapping(target = "arrivalTime.checkOut",source = "arrivalTime.checkOut")
    HotelReadDto map(Hotel hotel);

    @Named("shortenAddress")
    default String shortenAddress(Address address) {
        return address.getStreet() + ", "
                + address.getHouseNumber() + ", "
                + address.getCity() + ", "
                + address.getPostCode() + ", "
                + address.getCountry();
    }
}
