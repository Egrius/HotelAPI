package by.Egrius.demoTask.unit.service;

import by.Egrius.demoTask.dto.hotel.HotelCreateDto;
import by.Egrius.demoTask.dto.hotel.HotelReadDto;
import by.Egrius.demoTask.dto.hotel.HotelShortInfoReadDto;
import by.Egrius.demoTask.entity.Hotel;
import by.Egrius.demoTask.exception.HotelNotFoundException;
import by.Egrius.demoTask.mapper.HotelReadMapper;
import by.Egrius.demoTask.repository.HotelFilterRepository;
import by.Egrius.demoTask.repository.HotelHistogramRepository;
import by.Egrius.demoTask.repository.HotelRepository;
import by.Egrius.demoTask.repository.filter.HotelFilter;
import by.Egrius.demoTask.repository.projection.AmenityCountProjection;
import by.Egrius.demoTask.service.HotelService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceUnitTests {

    @Mock
    HotelRepository hotelRepository;

    @Mock
    HotelFilterRepository hotelFilterRepository;

    @Mock
    HotelHistogramRepository hotelHistogramRepository;

    @Mock
    HotelReadMapper hotelReadMapper;

    @InjectMocks
    HotelService hotelService;

    @Nested
    class GetAllHotelsShortInfoTests {

        @Test
        void shouldReturnMappedList_ifHotelExists() {
            // given
            Hotel mockHotel = Mockito.mock(Hotel.class);
            HotelShortInfoReadDto expected = new HotelShortInfoReadDto(
                    1L,
                    "testName",
                    "testDescription",
                    "testAddress",
                    "testPhone"
            );

            // when
            when(hotelRepository.findAll()).thenReturn(List.of(mockHotel));
            when(hotelReadMapper.mapToShortInfoReadDto(mockHotel)).thenReturn(expected);

            // then
            List<HotelShortInfoReadDto> actual = hotelService.getAllHotelsShortInfo();

            assertFalse(actual.isEmpty());
            assertThat(actual.size()).isEqualTo(1);

            assertEquals(expected.id(), actual.getFirst().id());

        }

        @Test
        void shouldReturnEmptyList_ifRepositoryIsEmpty() {
            // given
            when(hotelRepository.findAll()).thenReturn(List.of());

            // when
            List<HotelShortInfoReadDto> actual = hotelService.getAllHotelsShortInfo();

            // then
            assertThat(actual).isEmpty();
        }

        @Test
        void shouldNotCallMapperOnEmpty() {
            // given
            when(hotelRepository.findAll()).thenReturn(List.of());

            // when
            hotelService.getAllHotelsShortInfo();

            // then
            verifyNoInteractions(hotelReadMapper);
        }
    }

    @Nested
    class CreateHotelTests {

        @Test
        void shouldSaveAndReturnCorrectDto() {
            // given
            HotelCreateDto createDto = createDto();
            Hotel savedHotel = Hotel.builder().id(1L).build();
            HotelShortInfoReadDto expected = new HotelShortInfoReadDto(
                    1L, "testName", "testDescription", "testAddress", "testPhone"
            );

            when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);
            when(hotelReadMapper.mapToShortInfoReadDto(savedHotel)).thenReturn(expected);

            // when
            HotelShortInfoReadDto actual = hotelService.createHotel(createDto);

            // then
            assertEquals(expected.id(), actual.id());
            verify(hotelRepository).save(any(Hotel.class));
        }

        @Test
        void shouldSaveAllTheFields() {
            // given
            HotelCreateDto createDto = createDto();
            when(hotelRepository.save(any(Hotel.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            hotelService.createHotel(createDto);

            // then
            verify(hotelRepository).save(argThat(h ->
                    h.getName().equals(createDto.name())
                            && h.getDescription().equals(createDto.description())
                            && h.getBrand().equals(createDto.brand())
                            && h.getAddress().getHouseNumber().equals(createDto.address().houseNumber())
                            && h.getAddress().getStreet().equals(createDto.address().street())
                            && h.getAddress().getCity().equals(createDto.address().city())
                            && h.getAddress().getCountry().equals(createDto.address().country())
                            && h.getAddress().getPostCode().equals(createDto.address().postCode())
                            && h.getContacts().getPhone().equals(createDto.contacts().phone())
                            && h.getContacts().getEmail().equals(createDto.contacts().email())
                            && h.getArrivalTime().getCheckIn().equals(createDto.arrivalTime().checkIn())
                            && h.getArrivalTime().getCheckOut().equals(createDto.arrivalTime().checkOut())
            ));
        }

        @Test
        void shouldUseMapperOnSavedEntity() {
            // given
            HotelCreateDto createDto = createDto();
            Hotel savedHotel = Hotel.builder().id(99L).build();
            HotelShortInfoReadDto expected = new HotelShortInfoReadDto(
                    99L, "testName", "testDescription", "testAddress", "testPhone"
            );

            when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);
            when(hotelReadMapper.mapToShortInfoReadDto(savedHotel)).thenReturn(expected);

            // when
            HotelShortInfoReadDto actual = hotelService.createHotel(createDto);

            // then
            assertEquals(99L, actual.id());
            verify(hotelReadMapper).mapToShortInfoReadDto(savedHotel);
        }

        private HotelCreateDto createDto() {
            return new HotelCreateDto(
                    "testName",
                    "testDescription",
                    "testBrand",
                    new HotelCreateDto.AddressCreateDto(1, "testStreet", "testCity", "testCountry", "testPostCode"),
                    new HotelCreateDto.ContactsCreateDto("testPhone", "test@email.com"),
                    new HotelCreateDto.ArrivalTimeCreateDto(LocalTime.of(14, 0), LocalTime.of(12, 0))
            );
        }
    }

    @Nested
    class GetHotelByIdTests {

        @Test
        void shouldReturnsDtoWhenFound() {
            // given
            long id = 1L;
            Hotel mockHotel = Mockito.mock(Hotel.class);
            HotelReadDto expected = Mockito.mock(HotelReadDto.class);

            when(hotelRepository.findById(id)).thenReturn(Optional.of(mockHotel));
            when(hotelReadMapper.map(mockHotel)).thenReturn(expected);

            // when
            HotelReadDto actual = hotelService.getHotelById(id);

            // then
            assertThat(actual).isSameAs(expected);
            verify(hotelReadMapper).map(mockHotel);
        }

        @Test
        void shouldThrowWhenNotFound() {
            // given
            long id = 99L;
            when(hotelRepository.findById(id)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> hotelService.getHotelById(id))
                    .isInstanceOf(HotelNotFoundException.class);
        }
    }

    @Nested
    class AddAmenitiesTests {

        @Test
        void shouldAddNewAmenities() {
            // given
            long hotelId = 1L;
            Hotel hotel = Hotel.builder()
                    .id(hotelId)
                    .amenities(new HashSet<>(Set.of("Free WiFi")))
                    .build();

            when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

            // when
            hotelService.addAmenities(hotelId, List.of("Free parking", "Fitness center"));

            // then
            assertThat(hotel.getAmenities())
                    .containsExactlyInAnyOrder("Free WiFi", "Free parking", "Fitness center");
        }

        @Test
        void shouldIgnoresDuplicateAmenities() {
            // given
            long hotelId = 1L;
            Hotel hotel = Hotel.builder()
                    .id(hotelId)
                    .amenities(new HashSet<>(Set.of("Free WiFi")))
                    .build();

            when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

            // when
            hotelService.addAmenities(hotelId, List.of("Free WiFi", "Free parking"));

            // then
            assertThat(hotel.getAmenities())
                    .containsExactlyInAnyOrder("Free WiFi", "Free parking");
            assertThat(hotel.getAmenities()).hasSize(2);
        }

        @Test
        void shouldThrow_whenHotelNotFound() {
            // given
            long hotelId = 99L;
            when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> hotelService.addAmenities(hotelId, List.of("Free WiFi")))
                    .isInstanceOf(HotelNotFoundException.class);
        }

        @Test
        void shouldNotChangeAmenities_whenEmptyInput() {
            // given
            long hotelId = 1L;
            Hotel hotel = Hotel.builder()
                    .id(hotelId)
                    .amenities(new HashSet<>(Set.of("Free WiFi")))
                    .build();

            when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

            // when
            hotelService.addAmenities(hotelId, List.of());

            // then
            assertThat(hotel.getAmenities()).containsExactly("Free WiFi");
        }
    }

    @Nested
    class GetHistogramByParamTests {

        @Test
        void shouldUseHotelHistogramRepository_whenGettingHistogramByHotelParam() {
            // given
            String param = "city";
            when(hotelHistogramRepository.getHistogram(param)).thenReturn(Map.of("Minsk", 1L));

            // when
            Map<String, Long> actual = hotelService.getHistogramByParam(param);

            // then
            assertThat(actual).containsEntry("Minsk", 1L);
            verify(hotelHistogramRepository, times(1)).getHistogram(param);
            verifyNoInteractions(hotelRepository);
        }

        @Test
        void shouldUseHotelRepository_whenGettingHistogramByAmenity() {
            // given
            AmenityCountProjection wifi = projection("Free WiFi", 20L);
            AmenityCountProjection parking = projection("Free parking", 1L);
            when(hotelRepository.getHistogramByHotelAmenity()).thenReturn(List.of(wifi, parking));

            // when
            Map<String, Long> actual = hotelService.getHistogramByParam("amenities");

            // then
            assertThat(actual).hasSize(2);
            assertThat(actual).containsEntry("Free WiFi", 20L);
            assertThat(actual).containsEntry("Free parking", 1L);
            verify(hotelRepository).getHistogramByHotelAmenity();
            verifyNoInteractions(hotelHistogramRepository);
        }

        @Test
        void shouldKeepAmenitiesOrder() {
            // given
            AmenityCountProjection b = projection("B-amenity", 5L);
            AmenityCountProjection a = projection("A-amenity", 1L);
            when(hotelRepository.getHistogramByHotelAmenity()).thenReturn(List.of(b, a));

            // when
            Map<String, Long> actual = hotelService.getHistogramByParam("amenities");

            // then
            assertThat(actual).isInstanceOf(LinkedHashMap.class);
            assertThat(actual.keySet()).containsExactly("B-amenity", "A-amenity");
        }

        @Test
        void shouldThrow_whenUnsupportedParam() {
            // given
            String param = "stars";

            // when / then
            assertThatThrownBy(() -> hotelService.getHistogramByParam(param))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(param);

            verifyNoInteractions(hotelRepository, hotelHistogramRepository);
        }

        @Test
        void shouldThrowIllegalArgument_whenNullParam() {
            assertThatThrownBy(() -> hotelService.getHistogramByParam(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        private AmenityCountProjection projection(String amenity, long count) {
            AmenityCountProjection p = Mockito.mock(AmenityCountProjection.class);
            when(p.getAmenity()).thenReturn(amenity);
            when(p.getCount()).thenReturn(count);
            return p;
        }
    }

    @Nested
    class SearchTests {

        @Test
        void shouldDelegateAndMapToDto() {
            // given
            HotelFilter filter = Mockito.mock(HotelFilter.class);
            Hotel mockHotel = Mockito.mock(Hotel.class);
            HotelShortInfoReadDto expected = new HotelShortInfoReadDto(
                    1L, "testName", "testDescription", "testAddress", "testPhone"
            );

            when(hotelFilterRepository.findAllByFilter(filter)).thenReturn(List.of(mockHotel));
            when(hotelReadMapper.mapToShortInfoReadDto(mockHotel)).thenReturn(expected);

            // when
            List<HotelShortInfoReadDto> actual = hotelService.getHotelsByFilter(filter);

            // then
            assertThat(actual).hasSize(1);
            assertEquals(expected.id(), actual.getFirst().id());
        }

        @Test
        void shouldReturnEmpty_whenNoMatches() {
            // given
            HotelFilter filter = Mockito.mock(HotelFilter.class);
            when(hotelFilterRepository.findAllByFilter(filter)).thenReturn(List.of());

            // when
            List<HotelShortInfoReadDto> actual = hotelService.getHotelsByFilter(filter);

            // then
            assertThat(actual).isEmpty();
        }

        @Test
        void shouldPassFilterWithoutChanges() {
            // given
            HotelFilter filter = Mockito.mock(HotelFilter.class);
            when(hotelFilterRepository.findAllByFilter(filter)).thenReturn(List.of());

            // when
            hotelService.getHotelsByFilter(filter);

            // then
            verify(hotelFilterRepository).findAllByFilter(filter);
        }
    }
}