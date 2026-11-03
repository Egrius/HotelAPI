package by.Egrius.demoTask.slice.repository;

import by.Egrius.demoTask.entity.Hotel;
import by.Egrius.demoTask.entity.embeddable.Address;
import by.Egrius.demoTask.entity.embeddable.ArrivalTime;
import by.Egrius.demoTask.entity.embeddable.Contacts;
import by.Egrius.demoTask.repository.HotelHistogramRepository;
import by.Egrius.demoTask.repository.HotelHistogramRepositoryImpl;
import by.Egrius.demoTask.repository.HotelRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@DataJpaTest
@Import(HotelHistogramRepositoryImpl.class)
public class HotelHistogramRepositoryIntegrationTests {

    @Autowired
    private HotelHistogramRepository hotelHistogramRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Hotel hotel(String name, String brand, String city, String country) {
        return Hotel.builder()
                .name(name)
                .description("desc")
                .brand(brand)
                .address(Address.builder()
                        .houseNumber(1).street("St").city(city).country(country).postCode("123456")
                        .build())
                .contacts(Contacts.builder().phone("+375").email("e@test.com").build())
                .arrivalTime(ArrivalTime.builder()
                        .checkIn(LocalTime.of(14, 0)).checkOut(LocalTime.of(12, 0))
                        .build())
                .amenities(Collections.emptySet())
                .build();
    }

    private Hotel hotel(String name, String brand, String city, String country,
                        int houseNumber, String street, String postCode) {
        return Hotel.builder()
                .name(name)
                .description("desc")
                .brand(brand)
                .address(Address.builder()
                        .houseNumber(houseNumber).street(street).city(city).country(country).postCode(postCode)
                        .build())
                .contacts(Contacts.builder().phone("+375").email("e@test.com").build())
                .arrivalTime(ArrivalTime.builder()
                        .checkIn(LocalTime.of(14, 0)).checkOut(LocalTime.of(12, 0))
                        .build())
                .amenities(Collections.emptySet())
                .build();
    }

    private final String BRAND_HILTON = "Hilton";
    private final String BRAND_MARIOTT = "Marriott";

    private void setUp() {
        hotelRepository.saveAll(List.of(
                hotel("H1", BRAND_HILTON,   "Minsk",  "Belarus"),
                hotel("H2", BRAND_HILTON,   "Minsk",  "Belarus"),
                hotel("H3", BRAND_MARIOTT, "Moscow", "Russia"),
                hotel("H4", BRAND_HILTON,   "Moscow", "Russia")
        ));
        entityManager.flush();
    }

    @Nested
    class BrandHistogram {

        private final String param = "brand";

        @Test
        void shouldGroupByBrand() {
            setUp();

            Map<String, Long> result = hotelHistogramRepository.getHistogram(param);

            assertNotNull(result);
            assertThat(result.size()).isEqualTo(2);
            assertEquals(3, result.get(BRAND_HILTON));
            assertEquals(1, result.get(BRAND_MARIOTT));
        }

        @Test
        void shouldReturnSingleEntry_ifAllSameBrand() {

            hotelRepository.saveAll(List.of(
                    hotel("H1", BRAND_HILTON, "Minsk", "Belarus"),
                    hotel("H2", BRAND_HILTON, "Moscow", "Russia"),
                    hotel("H3", BRAND_HILTON, "Mogilev", "Belarus")
            ));
            entityManager.flush();

            Map<String, Long> result = hotelHistogramRepository.getHistogram(param);

            assertThat(result).hasSize(1);
            assertEquals(3L, result.get(BRAND_HILTON));
        }

        @Test
        void shouldThrow_whenParamCaseDiffers() {
            setUp();

            assertThatThrownBy(() -> hotelHistogramRepository.getHistogram("Brand"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class CityHistogram {

        private final String param = "city";

        @Test
        void shouldGroupByCityThroughEmbedded() {
            setUp();

            Map<String, Long> result = hotelHistogramRepository.getHistogram(param);

            assertNotNull(result);
            assertThat(result).hasSize(2);
            assertEquals(2L, result.get("Minsk"));
            assertEquals(2L, result.get("Moscow"));
        }

        @Test
        void shouldGroupOnlyByCityNotFullAddress() {
            hotelRepository.saveAll(List.of(
                    hotel("H1", BRAND_HILTON, "Minsk", "Belarus", 1, "Pobediteley Avenue", "123456"),
                    hotel("H2", BRAND_HILTON, "Minsk", "Belarus", 5, "Nezavisimosti Avenue", "654321")
            ));
            entityManager.flush();

            Map<String, Long> result = hotelHistogramRepository.getHistogram(param);

            assertThat(result).hasSize(1);
            assertEquals(2L, result.get("Minsk"));
        }
    }

    @Nested
    class CountryHistogram {

        private final String param = "country";

        @Test
        void shouldGroupByCountryThroughEmbedded() {
            setUp();

            Map<String, Long> result = hotelHistogramRepository.getHistogram(param);

            assertNotNull(result);
            assertThat(result).hasSize(2);
            assertEquals(2L, result.get("Belarus"));
            assertEquals(2L, result.get("Russia"));
        }

        @Test
        void shouldGroupByCountryIgnoringCity() {
            hotelRepository.saveAll(List.of(
                    hotel("H1", BRAND_HILTON, "Minsk", "Belarus"),
                    hotel("H2", BRAND_HILTON, "Mogilev", "Belarus"),
                    hotel("H3", BRAND_HILTON, "Moscow", "Russia")
            ));
            entityManager.flush();

            Map<String, Long> result = hotelHistogramRepository.getHistogram(param);

            assertThat(result).hasSize(2);
            assertEquals(2L, result.get("Belarus"));
            assertEquals(1L, result.get("Russia"));
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void shouldReturnEmptyWhenNoHotels() {

            String param = "brand";

            Map<String, Long> result = hotelHistogramRepository.getHistogram(param);

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        void shouldThrowForUnsupportedParam() {

            setUp();
            String param = "stars";

            assertThatThrownBy(() -> hotelHistogramRepository.getHistogram(param))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(param);
        }

        @Test
        void shouldThrowForNullParam() {

            assertThatThrownBy(() -> hotelHistogramRepository.getHistogram(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldOrderByParam() {
            String param = "brand";

            String brandA = "A";
            String brandB = "B";
            String brandC = "C";

            hotelRepository.saveAll(List.of(
                    hotel("H1", brandA, "Minsk", "Belarus"),
                    hotel("H2", brandC, "Mogilev", "Belarus"),
                    hotel("H3", brandB, "Moscow", "Russia")
            ));
            entityManager.flush();

            Map<String, Long> result = hotelHistogramRepository.getHistogram(param);

            assertThat(result.keySet()).containsExactly(brandA, brandB, brandC);
        }

        @Test
        void shouldHandleSingleHotel() {

            String param = "brand";
            String brand = "Solo";

            hotelRepository.saveAll(List.of(
                    hotel("H1", brand, "Minsk", "Belarus")
            ));
            entityManager.flush();

            Map<String, Long> result = hotelHistogramRepository.getHistogram(param);

            assertThat(result.size()).isEqualTo(1);
            assertThat(result.get(brand)).isEqualTo(1);
        }
    }
}