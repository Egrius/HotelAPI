package by.Egrius.demoTask.slice.repository;

import by.Egrius.demoTask.entity.Hotel;
import by.Egrius.demoTask.entity.embeddable.Address;
import by.Egrius.demoTask.entity.embeddable.ArrivalTime;
import by.Egrius.demoTask.entity.embeddable.Contacts;
import by.Egrius.demoTask.repository.HotelFilterRepositoryImpl;
import by.Egrius.demoTask.repository.HotelRepository;
import by.Egrius.demoTask.repository.filter.HotelFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@Import(HotelFilterRepositoryImpl.class)
public class HotelFilterRepositoryIntegrationTests {

    @Autowired
    private HotelFilterRepositoryImpl hotelFilterRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String HILTON_BRAND = "Hilton";
    private static final String MARRIOTT_BRAND = "Marriott";
    private static final String MINSK_CITY = "Minsk";
    private static final String MOSCOW_CITY = "Moscow";
    private static final String MOGILEV_CITY = "Mogilev";
    private static final String BELARUS_COUNTRY = "Belarus";
    private static final String RUSSIA_COUNTRY = "Russia";
    private static final String WIFI_AMENITY = "Free WiFi";
    private static final String PARKING_AMENITY = "Free Parking";
    private static final String FITNESS_AMENITY = "Fitness center";

    private Hotel hotel(String name, String brand, String city, String country, Set<String> amenities) {
        return Hotel.builder()
                .name(name).description("desc").brand(brand)
                .address(Address.builder().houseNumber(1).street("St")
                        .city(city).country(country).postCode("123456").build())
                .contacts(Contacts.builder().phone("+375").email("e@test.com").build())
                .arrivalTime(ArrivalTime.builder()
                        .checkIn(LocalTime.of(14, 0)).checkOut(LocalTime.of(12, 0)).build())
                .amenities(amenities)
                .build();
    }

    private void setUp() {
        hotelRepository.saveAll(List.of(
                hotel("DoubleTree",         HILTON_BRAND,   MINSK_CITY,   BELARUS_COUNTRY, Set.of(WIFI_AMENITY, PARKING_AMENITY)),
                hotel("Hilton Garden",      HILTON_BRAND,   MINSK_CITY,   BELARUS_COUNTRY, Set.of(WIFI_AMENITY)),
                hotel("Marriott Grand",     MARRIOTT_BRAND, MOSCOW_CITY,  RUSSIA_COUNTRY,  Set.of(FITNESS_AMENITY)),
                hotel("Marriott Tverskaya", MARRIOTT_BRAND, MOSCOW_CITY,  RUSSIA_COUNTRY,  Set.of(WIFI_AMENITY, FITNESS_AMENITY)),
                hotel("Budget Inn",         HILTON_BRAND,   MOGILEV_CITY, BELARUS_COUNTRY, Collections.emptySet())
        ));
        entityManager.flush();
    }

    @Test
    void shouldReturnAll_whenFilterEmpty() {
        setUp();
        List<Hotel> result = hotelFilterRepository.findAllByFilter(
                new HotelFilter(null, null, null, null, null));
        assertThat(result).hasSize(5);
        assertThat(result).extracting(Hotel::getId).isSorted();
    }

    @Test
    void shouldMatchName_partiallyIgnoringCase() {
        setUp();
        List<Hotel> result = hotelFilterRepository.findAllByFilter(
                new HotelFilter("hilton", null, null, null, null));
        assertThat(result).extracting(Hotel::getName)
                .containsExactly("Hilton Garden");
    }

    @Test
    void shouldMatchBrand_exactlyIgnoringCase() {
        setUp();

        assertThat(hotelFilterRepository.findAllByFilter(
                new HotelFilter(null, "hilton", null, null, null)))
                .hasSize(3);

        assertThat(hotelFilterRepository.findAllByFilter(
                new HotelFilter(null, "Hilt", null, null, null)))
                .isEmpty();
    }

    @Test
    void shouldMatchCity_throughEmbeddedAddress() {
        setUp();
        List<Hotel> result = hotelFilterRepository.findAllByFilter(
                new HotelFilter(null, null, "minsk", null, null));
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(h -> h.getAddress().getCity().equals(MINSK_CITY));
    }

    @Test
    void shouldMatchCountry_throughEmbeddedAddress() {
        setUp();
        List<Hotel> result = hotelFilterRepository.findAllByFilter(
                new HotelFilter(null, null, null, "belarus", null));
        assertThat(result).hasSize(3);
    }

    @Test
    void shouldMatchAmenity_ignoringCase() {
        setUp();
        List<Hotel> result = hotelFilterRepository.findAllByFilter(
                new HotelFilter(null, null, null, null, List.of("Free wiFi")));
        assertThat(result).hasSize(3);
    }

    @Test
    void shouldMatchAllAmenities_whenMultipleProvided() {
        setUp();
        List<Hotel> result = hotelFilterRepository.findAllByFilter(
                new HotelFilter(null, null, null, null, List.of(WIFI_AMENITY, PARKING_AMENITY)));
        assertThat(result).extracting(Hotel::getName).doesNotHaveDuplicates();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("DoubleTree");
    }

    @Test
    void shouldIgnoreAmenities_whenListIsEmpty() {
        setUp();
        List<Hotel> result = hotelFilterRepository.findAllByFilter(
                new HotelFilter(null, null, null, null, List.of()));
        assertThat(result).hasSize(5);
    }

    @Test
    void shouldCombineFiltersWithAnd() {
        setUp();
        assertThat(hotelFilterRepository.findAllByFilter(
                new HotelFilter(null, HILTON_BRAND, MINSK_CITY, null, null)))
                .hasSize(2);
        assertThat(hotelFilterRepository.findAllByFilter(
                new HotelFilter(null, MARRIOTT_BRAND, MINSK_CITY, null, null)))
                .isEmpty();
    }

    @Test
    void shouldReturnEmpty_whenNoMatch() {
        setUp();
        List<Hotel> result = hotelFilterRepository.findAllByFilter(
                new HotelFilter("Radisson", null, null, null, null));
        assertThat(result).isEmpty();
    }
}