package by.Egrius.demoTask.slice.controller;

import by.Egrius.demoTask.controller.ApiController;
import by.Egrius.demoTask.dto.hotel.HotelCreateDto;
import by.Egrius.demoTask.dto.hotel.HotelReadDto;
import by.Egrius.demoTask.dto.hotel.HotelShortInfoReadDto;
import by.Egrius.demoTask.exception.HotelNotFoundException;
import by.Egrius.demoTask.repository.filter.HotelFilter;
import by.Egrius.demoTask.service.HotelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ApiController.class)
public class ApiControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HotelService hotelService;

    private final HotelReadDto hotelReadDto = new HotelReadDto(
            1L,
            "DoubleTree by Hilton Minsk",
            "The DoubleTree by Hilton Hotel Minsk offers 193 luxurious rooms in the Belorussian capital and stunning views of Minsk city from the hotel's 20th floor ...",
            "Hilton",
            new HotelReadDto.AddressReadDto(
                    9,
                    "Pobediteley Avenue",
                    "Minsk",
                    "Belarus",
                    "220004"
            ),
            new HotelReadDto.ContactsReadDto(
                    "+375 17 309-80-00",
                    "doubletreeminsk.info@hilton.com"
            ),
            new HotelReadDto.ArrivalTimeReadDto(
                    LocalTime.of(14, 0),
                    LocalTime.of(12, 0)
            ),
            List.of(
                    "Free parking",
                    "Free WiFi",
                    "Non-smoking rooms",
                    "Concierge",
                    "On-site restaurant",
                    "Fitness center",
                    "Pet-friendly rooms",
                    "Room service",
                    "Business center",
                    "Meeting rooms"
            )
    );

    private final HotelShortInfoReadDto shortInfoReadDto = new HotelShortInfoReadDto(
            1L,
            "DoubleTree by Hilton Minsk",
            "The DoubleTree by Hilton Hotel Minsk offers 193 luxurious rooms in the Belorussian capital and stunning views of Minsk city from the hotel's 20th floor ...",
            "9 Pobediteley Avenue, Minsk, 220004, Belarus",
            "+375 17 309-80-00"
    );

    @Nested
    @DisplayName("GET /property-view/hotels")
    class GetHotels {

        private final String BASE = "/property-view/hotels";

        @Test
        void shouldReturn200AndArray_whenHotelsExist() throws Exception {
            when(hotelService.getAllHotelsShortInfo()).thenReturn(List.of(shortInfoReadDto));

            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(shortInfoReadDto.id()))
                    .andExpect(jsonPath("$[0].name").value(shortInfoReadDto.name()))
                    .andExpect(jsonPath("$[0].description").value(shortInfoReadDto.description()))
                    .andExpect(jsonPath("$[0].address").value(shortInfoReadDto.address()))
                    .andExpect(jsonPath("$[0].phone").value(shortInfoReadDto.phone()));
        }

        @Test
        void shouldReturn200AndEmptyArray_whenNoHotels() throws Exception {
            when(hotelService.getAllHotelsShortInfo()).thenReturn(List.of());

            mockMvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /property-view/hotels/{id}")
    class GetHotelById {

        private final String BASE = "/property-view/hotels/";

        @Test
        void shouldReturn200AndFullJson_whenFound() throws Exception {
            when(hotelService.getHotelById(hotelReadDto.id())).thenReturn(hotelReadDto);

            mockMvc.perform(get(BASE + hotelReadDto.id()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(hotelReadDto.id()))
                    .andExpect(jsonPath("$.name").value(hotelReadDto.name()))
                    .andExpect(jsonPath("$.description").value(hotelReadDto.description()))
                    .andExpect(jsonPath("$.brand").value(hotelReadDto.brand()))

                    // address
                    .andExpect(jsonPath("$.address").isMap())
                    .andExpect(jsonPath("$.address.houseNumber").value(hotelReadDto.address().houseNumber()))
                    .andExpect(jsonPath("$.address.street").value(hotelReadDto.address().street()))
                    .andExpect(jsonPath("$.address.city").value(hotelReadDto.address().city()))
                    .andExpect(jsonPath("$.address.country").value(hotelReadDto.address().country()))
                    .andExpect(jsonPath("$.address.postCode").value(hotelReadDto.address().postCode()))

                    // contacts
                    .andExpect(jsonPath("$.contacts").isMap())
                    .andExpect(jsonPath("$.contacts.phone").value(hotelReadDto.contacts().phone()))
                    .andExpect(jsonPath("$.contacts.email").value(hotelReadDto.contacts().email()))

                    // arrivalTime
                    .andExpect(jsonPath("$.arrivalTime").isMap())
                    .andExpect(jsonPath("$.arrivalTime.checkIn")
                            .value(hotelReadDto.arrivalTime().checkIn().format(DateTimeFormatter.ofPattern("HH:mm"))))
                    .andExpect(jsonPath("$.arrivalTime.checkOut")
                            .value(hotelReadDto.arrivalTime().checkOut().format(DateTimeFormatter.ofPattern("HH:mm"))))

                    // amenities
                    .andExpect(jsonPath("$.amenities").isArray())
                    .andExpect(jsonPath("$.amenities.length()").value(hotelReadDto.amenities().size()))
                    .andExpect(jsonPath("$.amenities[0]").value(hotelReadDto.amenities().get(0)))
                    .andExpect(jsonPath("$.amenities[1]").value(hotelReadDto.amenities().get(1)));
        }

        @Test
        void shouldReturn404_whenNotFound() throws Exception {
            long idToFail = 999L;

            when(hotelService.getHotelById(idToFail))
                    .thenThrow(new HotelNotFoundException("Could not find a hotel with id '" + idToFail + "'"));

            mockMvc.perform(get(BASE + idToFail))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors").isEmpty());

        }

        @Test
        void shouldReturn400_whenIdIsNotNumber() throws Exception {
            mockMvc.perform(get(BASE + "str"))
                    .andExpect(status().isBadRequest());

            verify(hotelService, never()).getHotelById(anyLong());
        }
    }

    @Nested
    @DisplayName("GET /property-view/search")
    class Search {

        private final String BASE = "/property-view/search";

        @Test
        void shouldReturn200AndArray_whenFilterMatches() throws Exception {

            Map<String, String> params = Map.of(
                    "name",  hotelReadDto.name(),
                    "brand", hotelReadDto.brand(),
                    "city", hotelReadDto.address().city(),
                    "country", hotelReadDto.address().country()
            );

            HotelFilter filter = new HotelFilter(
                            hotelReadDto.name(),
                            hotelReadDto.brand(),
                            hotelReadDto.address().city(),
                            hotelReadDto.address().country(),
                            null
            );

            when(hotelService.getHotelsByFilter(filter)).thenReturn(List.of(shortInfoReadDto));


            mockMvc.perform(get(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .params(MultiValueMap.fromSingleValue(params)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(shortInfoReadDto.id()))
                    .andExpect(jsonPath("$[0].name").value(shortInfoReadDto.name()))
                    .andExpect(jsonPath("$[0].description").value(shortInfoReadDto.description()))
                    .andExpect(jsonPath("$[0].address").value(shortInfoReadDto.address()))
                    .andExpect(jsonPath("$[0].phone").value(shortInfoReadDto.phone()));
        }

        @Test
        void shouldPassAllParamsToService() throws Exception {

            List<String> amenities =  List.of( "Free parking", "Free WiFi");

            Map<String, List<String>> params = Map.of(
                    "name",   List.of(hotelReadDto.name()),
                    "brand",  List.of(hotelReadDto.brand()),
                    "city",  List.of(hotelReadDto.address().city()),
                    "country",  List.of(hotelReadDto.address().country()),
                    "amenities",amenities
            );

            when(hotelService.getHotelsByFilter(any(HotelFilter.class))).thenReturn(List.of());

            mockMvc.perform(get(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .params(MultiValueMap.fromMultiValue(params)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(hotelService).getHotelsByFilter(
                    argThat(filter ->
                                filter.name().equals(hotelReadDto.name()) &&
                                filter.brand().equals(hotelReadDto.brand()) &&
                                filter.city().equals(hotelReadDto.address().city()) &&
                                filter.country().equals(hotelReadDto.address().country()) &&
                                filter.amenities() != null &&
                                filter.amenities().size() == amenities.size() &&
                                filter.amenities().containsAll(amenities)
                    )
            );
        }

        @Test
        void shouldReturn200AndEmptyArray_whenNoMatches() throws Exception {

            String unknownCity = "Test City";

            Map<String, String> params = Map.of("city",  unknownCity);

            when(hotelService.getHotelsByFilter(any(HotelFilter.class))).thenReturn(List.of());

            mockMvc.perform(get(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .params(MultiValueMap.fromSingleValue(params)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("POST /property-view/hotels")
    class CreateHotel {

        private final String BASE = "/property-view/hotels";

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void shouldReturn200AndShortInfo_whenBodyValid() throws Exception {
            HotelCreateDto createDto = new HotelCreateDto(
                    hotelReadDto.name(),
                    hotelReadDto.description(),
                    hotelReadDto.brand(),
                    new HotelCreateDto.AddressCreateDto(
                            hotelReadDto.address().houseNumber(),
                            hotelReadDto.address().street(),
                            hotelReadDto.address().city(),
                            hotelReadDto.address().country(),
                            hotelReadDto.address().postCode()
                    ),
                    new HotelCreateDto.ContactsCreateDto(
                            hotelReadDto.contacts().phone(),
                            hotelReadDto.contacts().email()
                    ),
                    new HotelCreateDto.ArrivalTimeCreateDto(
                            hotelReadDto.arrivalTime().checkIn(),
                            hotelReadDto.arrivalTime().checkOut()
                    )
            );

            when(hotelService.createHotel(any(HotelCreateDto.class))).thenReturn(shortInfoReadDto);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(shortInfoReadDto.id()))
                    .andExpect(jsonPath("$.name").value(shortInfoReadDto.name()))
                    .andExpect(jsonPath("$.description").value(shortInfoReadDto.description()))
                    .andExpect(jsonPath("$.address").value(shortInfoReadDto.address()))
                    .andExpect(jsonPath("$.phone").value(shortInfoReadDto.phone()));
        }

        @Test
        void shouldReturn400_whenNameIsBlank() throws Exception {
            HotelCreateDto dto = new HotelCreateDto(
                    "",
                    hotelReadDto.description(),
                    hotelReadDto.brand(),
                    new HotelCreateDto.AddressCreateDto(
                            hotelReadDto.address().houseNumber(),
                            hotelReadDto.address().street(),
                            hotelReadDto.address().city(),
                            hotelReadDto.address().country(),
                            hotelReadDto.address().postCode()
                    ),
                    new HotelCreateDto.ContactsCreateDto(
                            hotelReadDto.contacts().phone(),
                            hotelReadDto.contacts().email()
                    ),
                    new HotelCreateDto.ArrivalTimeCreateDto(
                            hotelReadDto.arrivalTime().checkIn(),
                            hotelReadDto.arrivalTime().checkOut()
                    )
            );

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[?(@.field=='name')]").exists());
        }

        @Test
        void shouldReturn400_whenBodyEmpty() throws Exception {
            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors").isNotEmpty())
                    .andExpect(jsonPath("$.errors[?(@.field=='name')]").exists())
                    .andExpect(jsonPath("$.errors[?(@.field=='brand')]").exists())
                    .andExpect(jsonPath("$.errors[?(@.field=='address')]").exists())
                    .andExpect(jsonPath("$.errors[?(@.field=='contacts')]").exists())
                    .andExpect(jsonPath("$.errors[?(@.field=='arrivalTime')]").exists());
        }

        @Test
        void shouldReturn400_whenHouseNumberMissing() throws Exception {
            String json = """
                {
                  "name": "DoubleTree by Hilton Minsk",
                  "description": "desc",
                  "brand": "Hilton",
                  "address": {
                    "street": "Pobediteley Avenue",
                    "city": "Minsk",
                    "country": "Belarus",
                    "postCode": "220004"
                  },
                  "contacts": {
                    "phone": "+375 17 309-80-00",
                    "email": "doubletreeminsk.info@hilton.com"
                  },
                  "arrivalTime": {
                    "checkIn": "14:00"
                  }
                }
                """;

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[?(@.field=='address.houseNumber')]").exists());
        }
    }

    @Nested
    @DisplayName("POST /property-view/hotels/{id}/amenities")
    class AddAmenities {

        private final String BASE = "/property-view/hotels";

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void shouldReturn200_whenAmenitiesValid() throws Exception {

            doNothing().when(hotelService).addAmenities(anyLong(), anyList());

            mockMvc.perform(post(BASE + "/1/amenities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of("Free WiFi"))))
                    .andExpect(status().isOk());

            verify(hotelService).addAmenities(eq(1L), eq(List.of("Free WiFi")));
        }

        @Test
        void shouldReturn400_whenListEmpty() throws Exception {

            mockMvc.perform(post(BASE + "/1/amenities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[]"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[?(@.field=='amenities')]").exists());
        }

        @Test
        void shouldReturn400_whenListContainsBlank() throws Exception {

            mockMvc.perform(post(BASE + "/1/amenities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"\", \"Free WiFi\"]"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[?(@.field=='amenities')]").exists());
        }

        @Test
        void shouldReturn404_whenHotelNotFound() throws Exception {
            long idToFail = 999L;
            List<String> amenities = List.of(hotelReadDto.amenities().getFirst());

            String errorString = "Could not find a hotel with id '" + idToFail + "'";

            doThrow(new HotelNotFoundException(errorString))
                    .when(hotelService).addAmenities(eq(idToFail), anyList());

            mockMvc.perform(post(BASE + "/" + idToFail + "/amenities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(amenities)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.message").value(errorString))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /property-view/histogram/{param}")
    class GetHistogram {

        private final String BASE = "/property-view/histogram";

        @Test
        void shouldReturn200AndMap_whenParamValid() throws Exception {
            String param = "city";
            Map<String, Long> histogram = Map.of("Minsk", 2L, "Moscow", 1L);

            when(hotelService.getHistogramByParam(param)).thenReturn(histogram);

            mockMvc.perform(get(BASE + "/" + param))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.Minsk").value(histogram.get("Minsk")))
                    .andExpect(jsonPath("$.Moscow").value(histogram.get("Moscow")));
        }

        @Test
        void shouldReturn400_whenParamUnsupported() throws Exception {
            String param = "stars";

            when(hotelService.getHistogramByParam(param))
                    .thenThrow(new IllegalArgumentException("Parameter '" + param + "' is not available"));

            mockMvc.perform(get(BASE + "/" + param))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.message").value("Parameter '" + param + "' is not available"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        void shouldReturn200AndEmptyMap_whenNoHotels() throws Exception {
            when(hotelService.getHistogramByParam("city")).thenReturn(Map.of());

            mockMvc.perform(get(BASE + "/city"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isMap())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}