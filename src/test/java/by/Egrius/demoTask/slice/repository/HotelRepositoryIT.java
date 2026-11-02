package by.Egrius.demoTask.slice.repository;

import by.Egrius.demoTask.entity.Hotel;
import by.Egrius.demoTask.entity.embeddable.Address;
import by.Egrius.demoTask.entity.embeddable.ArrivalTime;
import by.Egrius.demoTask.entity.embeddable.Contacts;
import by.Egrius.demoTask.repository.HotelRepository;
import by.Egrius.demoTask.repository.projection.AmenityCountProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@DataJpaTest
public class HotelRepositoryIT {

    @Autowired
    private HotelRepository hotelRepository;

    private Hotel hotel(String name, String brand, String city, String country, Set<String> amenities) {
        return Hotel.builder()
                .name(name)
                .description("desc")
                .brand(brand)
                .address(Address.builder()
                        .houseNumber(1).street("St").city(city).country(country).postCode("123456")
                        .build())
                .contacts(Contacts.builder().phone("+375").email("email@test.com").build())
                .arrivalTime(ArrivalTime.builder()
                        .checkIn(LocalTime.of(14, 0)).checkOut(LocalTime.of(12, 0))
                        .build())
                .amenities(amenities)
                .build();
    }

    @Nested
    class SaveAndFind {

        private final String TEST_NAME = "TestHotel";
        private final String TEST_BRAND = "Test Brand";
        private final String TEST_CITY = "Test City";
        private final String TEST_COUNTRY = "Test Country";

        @PersistenceContext
        private EntityManager entityManager;

        @Test
        void shouldSaveAndReadEmbeddedFields() {

            Hotel hotel = hotel(
                    TEST_NAME,
                    TEST_BRAND,
                    TEST_CITY,
                    TEST_COUNTRY,
                    Collections.emptySet()
            );
            Hotel saved =  hotelRepository.save(hotel);

            entityManager.flush();
            entityManager.clear();

            Hotel found = hotelRepository.findById(saved.getId()).orElseThrow();

            assertNotNull(found.getId());
            assertNotNull(found.getAddress());
            assertNotNull(found.getContacts());
            assertNotNull(found.getArrivalTime());
            assertNotNull(found.getAmenities());

            assertEquals(TEST_NAME, found.getName());
            assertEquals(TEST_BRAND, found.getBrand());
            assertEquals(TEST_CITY, found.getAddress().getCity());
            assertEquals(TEST_COUNTRY, found.getAddress().getCountry());
            assertThat(found.getAmenities()).isEmpty();

        }

        @Test
        void shouldSaveAndReadAmenities() {

            Set<String> amenities = Set.of("Free WiFi", "Free parking");

            Hotel hotel = hotel(
                    TEST_NAME,
                    TEST_BRAND,
                    TEST_CITY,
                    TEST_COUNTRY,
                    amenities
            );

            Hotel saved =  hotelRepository.save(hotel);

            entityManager.flush();
            entityManager.clear();

            Hotel found = hotelRepository.findById(saved.getId()).orElseThrow();

            assertNotNull(found.getId());
            assertNotNull(found.getAmenities());

            assertThat(found.getAmenities()).containsAll(amenities);
        }


        @Test
        void shouldNotDuplicateAmenityForSameHotel() {
            Hotel saved = hotelRepository.save(
                    hotel("H", "B", "Minsk", "Belarus", Set.of("Free WiFi")));
            entityManager.flush();

            assertThatThrownBy(() -> {
                entityManager.createNativeQuery(
                                "INSERT INTO hotel_amenities (hotel_id, amenity) VALUES (:id, :amenity)")
                        .setParameter("id", saved.getId())
                        .setParameter("amenity", "Free WiFi")
                        .executeUpdate();
                entityManager.flush();
            }).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
        }

        @Test
        @DisplayName("deleting hotel cascades to hotel_amenities")
        void shouldCascadeDeleteAmenities() {
            // given: hotel с amenities, save + flush + clear
            Set<String> amenities = Set.of("Free WiFi", "Free parking");

            Hotel hotel = hotel(
                    TEST_NAME,
                    TEST_BRAND,
                    TEST_CITY,
                    TEST_COUNTRY,
                    amenities
            );

            Hotel saved =  hotelRepository.save(hotel);

            hotelRepository.deleteById(saved.getId());
            entityManager.flush();
            entityManager.clear();

            Query query = entityManager.createNativeQuery(
                    "SELECT amenity FROM hotel_amenities WHERE hotel_id = :id", String.class);
            query.setParameter("id", saved.getId());

            assertThat(query.getResultList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getHistogramByHotelAmenity")
    class HistogramByAmenity {

        private final String FIRST_HOTEL_NAME = "TestHotel_1";
        private final String FIRST_HOTEL_BRAND = "Hilton";
        private final String FIRST_HOTEL_CITY = "Minsk";
        private final String FIRST_HOTEL_COUNTRY = "Belarus";

        private final String SECOND_HOTEL_NAME = "TestHotel_2";
        private final String SECOND_HOTEL_BRAND = "Hilton";
        private final String SECOND_HOTEL_CITY = "Moscow";
        private final String SECOND_HOTEL_COUNTRY = "Russia";

        private final String THIRD_HOTEL_NAME = "TestHotel_3";
        private final String THIRD_HOTEL_BRAND = "TEST_BRAND";
        private final String THIRD_HOTEL_CITY = "Minsk";
        private final String THIRD_HOTEL_COUNTRY = "Belarus";

        private final String FREE_WIFI_AMENITY = "Free WiFi";
        private final String FREE_PARKING_AMENITY = "Free Parking";

        @PersistenceContext
        private EntityManager entityManager;

        @BeforeEach
        void setUp() {

            Hotel firstHotel = hotel(
                    FIRST_HOTEL_NAME,
                    FIRST_HOTEL_BRAND,
                    FIRST_HOTEL_CITY,
                    FIRST_HOTEL_COUNTRY,
                    Set.of(FREE_WIFI_AMENITY)
            );

            Hotel secondHotel = hotel(
                    SECOND_HOTEL_NAME,
                    SECOND_HOTEL_BRAND,
                    SECOND_HOTEL_CITY,
                    SECOND_HOTEL_COUNTRY,
                    Set.of(FREE_WIFI_AMENITY)
            );

            Hotel thirdHotel = hotel(
                    THIRD_HOTEL_NAME,
                    THIRD_HOTEL_BRAND,
                    THIRD_HOTEL_CITY,
                    THIRD_HOTEL_COUNTRY,
                    Set.of(FREE_PARKING_AMENITY)
            );

            hotelRepository.saveAll(List.of(firstHotel, secondHotel, thirdHotel));
            hotelRepository.flush();
        }

        @Test
        void shouldGroupByAmenity() {
            List<AmenityCountProjection> result = hotelRepository.getHistogramByHotelAmenity();

            assertNotNull(result);
            assertThat(result).hasSize(2);

            assertThat(result.get(0).getAmenity()).isEqualTo(FREE_PARKING_AMENITY);
            assertThat(result.get(1).getAmenity()).isEqualTo(FREE_WIFI_AMENITY);

            assertThat(result.get(0).getCount()).isEqualTo(1);
            assertThat(result.get(1).getCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("returns empty list when no amenities")
        void shouldReturnEmptyWhenNoAmenities() {

            hotelRepository.deleteAll();
            hotelRepository.flush();

            hotelRepository.save(hotel("NoAmenities", "Brand", "City", "Country", Collections.emptySet()));
            hotelRepository.flush();

            List<AmenityCountProjection> result = hotelRepository.getHistogramByHotelAmenity();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("orders by amenity alphabetically")
        void shouldOrderByAmenity() {

            hotelRepository.deleteAll();
            hotelRepository.flush();

            hotelRepository.saveAll(List.of(
                    hotel("A", "Brand", "City", "Country", Set.of("C_amenity")),
                    hotel("B", "Brand", "City", "Country", Set.of("A_amenity")),
                    hotel("C", "Brand", "City", "Country", Set.of("B_amenity"))
            ));
            hotelRepository.flush();

            List<AmenityCountProjection> result = hotelRepository.getHistogramByHotelAmenity();

            assertThat(result).hasSize(3);
            assertThat(result).extracting(AmenityCountProjection::getAmenity)
                    .containsExactly("A_amenity", "B_amenity", "C_amenity");
        }
    }
}