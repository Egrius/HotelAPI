package by.Egrius.demoTask.service;

import by.Egrius.demoTask.dto.hotel.HotelCreateDto;
import by.Egrius.demoTask.dto.hotel.HotelReadDto;
import by.Egrius.demoTask.dto.hotel.HotelShortInfoReadDto;
import by.Egrius.demoTask.entity.Hotel;
import by.Egrius.demoTask.entity.embeddable.Address;
import by.Egrius.demoTask.entity.embeddable.ArrivalTime;
import by.Egrius.demoTask.entity.embeddable.Contacts;
import by.Egrius.demoTask.mapper.HotelReadMapper;
import by.Egrius.demoTask.repository.HotelFilterRepository;
import by.Egrius.demoTask.repository.HotelHistogramRepository;
import by.Egrius.demoTask.repository.HotelRepository;
import by.Egrius.demoTask.repository.filter.HotelFilter;
import by.Egrius.demoTask.repository.projection.AmenityCountProjection;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelFilterRepository hotelFilterRepository;
    private final HotelHistogramRepository hotelHistogramRepository;

    private final HotelReadMapper hotelReadMapper;

    private static final Set<String> AVAILABLE_PARAMS = Set.of("brand", "city", "country", "amenities");

    // 1)
    public List<HotelShortInfoReadDto> getAllHotelsShortInfo() {
        List<Hotel> hotels = hotelRepository.findAll();

        return hotels.stream().map(hotelReadMapper::mapToShortInfoReadDto).toList();
    }

    // 2)
    public HotelReadDto getHotelById(long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Could not find a hotel with id '" + id + "'"));

        return hotelReadMapper.map(hotel);
    }

    // 3)
    public List<HotelShortInfoReadDto> getHotelsByFilter(HotelFilter filter) {
        log.info("Got filter: " + filter);

        return hotelFilterRepository.findAllByFilter(filter).stream().map(hotelReadMapper::mapToShortInfoReadDto).toList();
    }

    // 4)
    // Нет бизнес ключа, чтобы не допускать дубликатов по содержанию
    @Transactional
    public HotelShortInfoReadDto createHotel(HotelCreateDto createDto) {

        Hotel hotel = Hotel.builder()
                .name(createDto.name())
                .description(createDto.description())
                .brand(createDto.brand())
                .address(Address.builder()
                        .houseNumber(createDto.address().houseNumber())
                        .street(createDto.address().street())
                        .city(createDto.address().city())
                        .country(createDto.address().country())
                        .postCode(createDto.address().postCode())
                        .build())
                .contacts(Contacts.builder()
                        .phone(createDto.contacts().phone())
                        .email(createDto.contacts().email())
                        .build())
                .arrivalTime(ArrivalTime.builder()
                        .checkIn(createDto.arrivalTime().checkIn())
                        .checkOut(createDto.arrivalTime().checkOut())
                        .build())
                .build();

        Hotel saved = hotelRepository.save(hotel);

        return hotelReadMapper.mapToShortInfoReadDto(saved);
    }

    // 5)
    @Transactional
    public void addAmenities(long hotelId, List<String> amenities) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new EntityNotFoundException("Could not find a hotel with id '" + hotelId + "'"));

        Set<String> hotelAmenities = hotel.getAmenities();

        hotelAmenities.addAll(amenities);
    }

    // 6)
    public Map<String, Long> getHistogramByParam(String param) {
        if(param == null) throw new IllegalArgumentException("Parameter can't be null");

        if(!AVAILABLE_PARAMS.contains(param)) throw new IllegalArgumentException("Parameter '" + param + "' is not available");

        Map<String, Long> result;

        if(param.equals("amenities")) {
            result = new LinkedHashMap<>();

            for (AmenityCountProjection p : hotelRepository.getHistogramByHotelAmenity()) {
                result.put(p.getAmenity(), p.getCount());
            }

        } else {
            result = hotelHistogramRepository.getHistogram(param);
        }

        return result;
    }
}
