package by.Egrius.demoTask.integration.service;

import by.Egrius.demoTask.dto.hotel.HotelCreateDto;
import by.Egrius.demoTask.dto.hotel.HotelReadDto;
import by.Egrius.demoTask.dto.hotel.HotelShortInfoReadDto;
import by.Egrius.demoTask.entity.Hotel;
import by.Egrius.demoTask.entity.embeddable.Address;
import by.Egrius.demoTask.entity.embeddable.ArrivalTime;
import by.Egrius.demoTask.entity.embeddable.Contacts;
import by.Egrius.demoTask.exception.HotelNotFoundException;
import by.Egrius.demoTask.repository.HotelRepository;
import by.Egrius.demoTask.repository.filter.HotelFilter;
import by.Egrius.demoTask.service.HotelService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class HotelServiceIntegrationTests {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelRepository hotelRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String HILTON_BRAND = "Hilton";
    private static final String MARRIOTT_BRAND = "Marriott";
    private static final String MINSK_CITY = "Minsk";
    private static final String MOSCOW_CITY = "Moscow";
    private static final String BELARUS_COUNTRY = "Belarus";
    private static final String RUSSIA_COUNTRY = "Russia";
    private static final String WIFI_AMENITY = "Free WiFi";
    private static final String PARKING_AMENITY = "Free Parking";

    private static final String PHONE = "+375";

    private Long hotelDoubleTreeId;
    private Long hotelHiltonGardenId;
    private Long hotelMarriottId;

    private Hotel hotel(String name, String brand, String city, String country, Set<String> amenities) {
        return Hotel.builder()
                .name(name).description("desc").brand(brand)
                .address(Address.builder().houseNumber(1).street("St")
                        .city(city).country(country).postCode("123456").build())
                .contacts(Contacts.builder().phone(PHONE).email("e@test.com").build())
                .arrivalTime(ArrivalTime.builder()
                        .checkIn(LocalTime.of(14, 0)).checkOut(LocalTime.of(12, 0)).build())
                .amenities(amenities)
                .build();
    }

    private void setUp() {
         hotelDoubleTreeId = hotelRepository.save(hotel("DoubleTree",         HILTON_BRAND,   MINSK_CITY,   BELARUS_COUNTRY, new HashSet<>(Set.of(WIFI_AMENITY, PARKING_AMENITY))))
                 .getId();

         hotelHiltonGardenId = hotelRepository.save(hotel("Hilton Garden",      HILTON_BRAND,   MINSK_CITY,   BELARUS_COUNTRY, new HashSet<>(Set.of(WIFI_AMENITY))))
                 .getId();

         hotelMarriottId = hotelRepository.save(hotel("Marriott",     MARRIOTT_BRAND, MOSCOW_CITY,  RUSSIA_COUNTRY,  new HashSet<>(Set.of(WIFI_AMENITY))))
                 .getId();

        entityManager.flush();
    }

    @Nested
    class GetAllHotelsShortInfo {

        @Test
        void shouldReturnMappedList() {

            setUp();

            List<HotelShortInfoReadDto> result = hotelService.getAllHotelsShortInfo();

            assertNotNull(result);
            assertEquals(3, result.size());

            assertThat(result).extracting(dto -> dto.address())
                    .allMatch(addr -> addr instanceof String);
            assertThat(result).extracting(dto -> dto.phone())
                    .allMatch(phone -> phone.equals(PHONE));
        }

        @Test
        void shouldReturnEmpty_whenNoHotels() {

            List<HotelShortInfoReadDto> result = hotelService.getAllHotelsShortInfo();

            assertNotNull(result);
            assertThat(result).isEmpty();
        }
    }


    @Nested
    class GetHotelById {

        @Test
        void shouldReturnFullDto() {
            setUp();

            HotelReadDto result = hotelService.getHotelById(hotelDoubleTreeId);
            Hotel fromDB = hotelRepository.findById(hotelDoubleTreeId).orElseThrow();

            assertNotNull(result);
            assertEquals(fromDB.getId(), result.id());
            assertEquals(fromDB.getName(), result.name());
            assertEquals(fromDB.getAddress().getCity(), result.address().city());
            assertEquals(fromDB.getAddress().getCountry(), result.address().country());
            assertEquals(fromDB.getContacts().getEmail(), result.contacts().email());
            assertEquals(fromDB.getArrivalTime().getCheckIn(), result.arrivalTime().checkIn());
            assertThat(result.amenities()).containsExactlyInAnyOrder(WIFI_AMENITY, PARKING_AMENITY);
        }

        @Test
        void shouldThrow_whenNotFound() {
            assertThatThrownBy(() -> hotelService.getHotelById(999L))
                    .isInstanceOf(HotelNotFoundException.class);
        }
    }


    @Nested
    class GetHotelsByFilter {

        @Test
        void shouldFilterByCity() {
            setUp();

            List<HotelShortInfoReadDto> result = hotelService.getHotelsByFilter(
                    new HotelFilter(null, null, MINSK_CITY, null, null));

            assertThat(result).hasSize(2);
        }

        @Test
        void shouldReturnAll_whenFilterEmpty() {
            setUp();

            List<HotelShortInfoReadDto> result = hotelService.getHotelsByFilter(
                    new HotelFilter(null, null, null, null, null));

            assertThat(result).hasSize(3);
        }

        @Test
        void shouldReturnEmpty_whenNoMatch() {
            setUp();

            List<HotelShortInfoReadDto> result = hotelService.getHotelsByFilter(
                    new HotelFilter(null, null, "Gomel", null, null));

            assertThat(result).isEmpty();
        }
    }


    @Nested
    class CreateHotel {

        @Test
        void shouldSaveAndReturnShortInfo() {
            HotelCreateDto dto = new HotelCreateDto(
                    "New Hotel",
                    "desc",
                    "New Brand",
                    new HotelCreateDto.AddressCreateDto(1, "St", MINSK_CITY, BELARUS_COUNTRY, "123456"),
                    new HotelCreateDto.ContactsCreateDto(PHONE, "new@test.com"),
                    new HotelCreateDto.ArrivalTimeCreateDto(LocalTime.of(15, 0), null)
            );

            HotelShortInfoReadDto result = hotelService.createHotel(dto);

            entityManager.flush();
            entityManager.clear();

            assertNotNull(result);
            assertNotNull(result.id());

            Hotel found = hotelRepository.findById(result.id()).orElseThrow();
            assertEquals("New Hotel", found.getName());
            assertEquals("desc", found.getDescription());
            assertEquals("New Brand", found.getBrand());
            assertEquals(MINSK_CITY, found.getAddress().getCity());
            assertEquals(PHONE, found.getContacts().getPhone());
            assertEquals(LocalTime.of(15, 0), found.getArrivalTime().getCheckIn());
            assertThat(found.getArrivalTime().getCheckOut()).isNull();
        }
    }


    @Nested
    class AddAmenities {

        @Test
        void shouldAddAmenities() {
            setUp();

            hotelService.addAmenities(hotelMarriottId, List.of(PARKING_AMENITY));

            entityManager.flush();
            entityManager.clear();

            Hotel found = hotelRepository.findById(hotelMarriottId).orElseThrow();
            assertThat(found.getAmenities())
                    .containsExactlyInAnyOrder(WIFI_AMENITY, PARKING_AMENITY);
        }

        @Test
        void shouldIgnoreDuplicates() {
            setUp();

            hotelService.addAmenities(hotelDoubleTreeId, List.of(WIFI_AMENITY));

            entityManager.flush();
            entityManager.clear();

            Hotel found = hotelRepository.findById(hotelDoubleTreeId).orElseThrow();
            assertThat(found.getAmenities()).hasSize(2);
        }

        @Test
        void shouldThrow_whenNotFound() {
            assertThatThrownBy(() -> hotelService.addAmenities(999L, List.of(WIFI_AMENITY)))
                    .isInstanceOf(HotelNotFoundException.class);
        }
    }


    @Nested
    class GetHistogramByParam {

        @Test
        void shouldReturnHistogramForSimpleParams() {
            setUp();

            assertThat(hotelService.getHistogramByParam("brand"))
                    .containsEntry(HILTON_BRAND, 2L)
                    .containsEntry(MARRIOTT_BRAND, 1L);

            assertThat(hotelService.getHistogramByParam("city"))
                    .containsEntry(MINSK_CITY, 2L)
                    .containsEntry(MOSCOW_CITY, 1L);

            assertThat(hotelService.getHistogramByParam("country"))
                    .containsEntry(BELARUS_COUNTRY, 2L)
                    .containsEntry(RUSSIA_COUNTRY, 1L);
        }

        @Test
        void shouldReturnHistogramForAmenities() {
            setUp();

            Map<String, Long> result = hotelService.getHistogramByParam("amenities");

            assertThat(result)
                    .containsEntry(WIFI_AMENITY, 3L)
                    .containsEntry(PARKING_AMENITY, 1L);
            assertThat(result).hasSize(2);
        }

        @Test
        void shouldReturnEmpty_whenNoHotels() {
            assertNotNull(hotelService.getHistogramByParam("city"));
            assertThat(hotelService.getHistogramByParam("city")).isEmpty();
        }

        @Test
        void shouldThrow_whenNull() {
            assertThatThrownBy(() -> hotelService.getHistogramByParam(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrow_whenUnsupported() {
            assertThatThrownBy(() -> hotelService.getHistogramByParam("stars"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("stars");
        }
    }
}
