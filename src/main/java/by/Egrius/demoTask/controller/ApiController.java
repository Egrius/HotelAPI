package by.Egrius.demoTask.controller;

import by.Egrius.demoTask.dto.hotel.HotelCreateDto;
import by.Egrius.demoTask.dto.hotel.HotelReadDto;
import by.Egrius.demoTask.dto.hotel.HotelShortInfoReadDto;
import by.Egrius.demoTask.dto.response.ErrorResponse;
import by.Egrius.demoTask.repository.filter.HotelFilter;
import by.Egrius.demoTask.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/property-view")
@RequiredArgsConstructor
@Tag(name = "ApiController", description = "Операции с отелями")
public class ApiController {

    private final HotelService hotelService;

    // имело бы смысл добавить пагинацию
    // 1) Короткая информация о всех отелях
    @Operation(
            summary = "Список отелей (краткая информация HotelShortInfoReadDto)",
            description = "Возвращает список всех отелей с краткой информацией: id, name, description, address (одной строкой), phone"
    )
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = HotelShortInfoReadDto.class))))
    @GetMapping("/hotels")
    public List<HotelShortInfoReadDto> getHotelsShortInfo() {
        return hotelService.getAllHotelsShortInfo();
    }

    // 2) Расширенная информация по конкретному отелю
    @Operation(
            summary = "Информация о конкретном отеле",
            description = "Возвращает полную информацию по конкретному отелю: brand, address, contacts, arrivalTime, amenities"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = HotelReadDto.class))),
            @ApiResponse(responseCode = "404", description = "Отель не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/hotels/{id}")
    public HotelReadDto getHotelsInfo(
            @Parameter(description = "ID отеля", example = "1")
            @PathVariable("id") Long id
    ) {
        return hotelService.getHotelById(id);
    }

    /*
    3) поиск получение списка всех отелей с их КРАТКОЙ информацией по следующим параметрам: name, brand, city, country, amenities.
	Например: /search?city=minsk
     */
    @Operation(
            summary = "Поиск отелей",
            description = "Возвращает список отелей (краткая информация) по фильтрам. " +
                    "Все параметры фильтра опциональны. " +
                    "Фильтр по amenities учитывает ВСЕ совпадения с переданным массивом amenities " +
                    "(если переданы 2 amenities, то ищет те отели, у которых как минимум 2 этих amenities, а не хотя бы один)"
    )
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = HotelShortInfoReadDto.class))))
    @GetMapping("/search")
    public List<HotelShortInfoReadDto> getHotelsShortInfoByFilter(
            @Parameter(description = "Совпадение по имени. Допускается также и частичное совпадение", example = "hilton")
            @RequestParam(value = "name", required = false) String name,

            @Parameter(description = "Точное совпадение по брендy. К регистру не чувствителен", example = "Hilton")
            @RequestParam(value = "brand", required = false) String brand,

            @Parameter(description = "Точное совпадение по городу. К регистру не чувствителен", example = "Minsk")
            @RequestParam(value = "city", required = false) String city,

           @Parameter(description = "Точное совпадение по стране. К регистру не чувствителен", example = "Belarus")
           @RequestParam(value = "country", required = false) String country,

           @Parameter(description = "Список amenities; отель подходит, если имеет ВСЕ указанные")
           @RequestParam(value = "amenities", required = false) List<String> amenities
    ) {

        HotelFilter filter = new HotelFilter(name, brand, city, country, amenities);

        return hotelService.getHotelsByFilter(filter);
    }

    /*
    6) получение колличества отелей сгруппированных по каждому значению указанного параметра.
    Параметр: brand, city, country, amenities
     */
    @Operation(
            summary = "Гистограмма по параметру",
            description = "Возвращает количество отелей, сгруппированных по значениям параметра. Допустимые параметры: brand, city, country, amenities"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Map.class, example = "{\"Minsk\": 2, \"Moscow\": 1}"))),
            @ApiResponse(responseCode = "400", description = "Неизвестный параметр",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/histogram/{param}")
    public Map<String, Long> getHistogramByParameter(
            @Parameter(description = "Параметр группировки", example = "city",
                    schema = @Schema(allowableValues = {"brand", "city", "country", "amenities"}))
            @PathVariable("param") String param
    ) {

        return hotelService.getHistogramByParam(param);
    }

    // 4) создание нового отеля
    @Operation(
            summary = "Создать отель",
            description = "Создаёт новый отель. Поля description и arrivalTime.checkOut опциональные"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Отель создан",
                    content = @Content(schema = @Schema(implementation = HotelShortInfoReadDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/hotels")
    public HotelShortInfoReadDto createHotel(
            @Valid @RequestBody HotelCreateDto hotelCreateDto
    ) {
        return hotelService.createHotel(hotelCreateDto);
    }

    // 5) добавление списка amenities к отелю
    @Operation(
            summary = "Добавить amenities к отелю",
            description = "Добавляет список удобств к существующему отелю. Дубликаты игнорируются (Set в сервисе)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amenities добавлены"),
            @ApiResponse(responseCode = "400", description = "Пустой список или blank-элемент",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Отель не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/hotels/{id}/amenities", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addHotelAmenities(
            @Parameter(description = "ID отеля", example = "1")
            @PathVariable("id") Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Список amenities", required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class,
                            example = "Free WiFi"))))
            @Valid @NotEmpty @RequestBody List<@NotBlank @Size(max = 255) String> amenities
    ) {

        hotelService.addAmenities(id, amenities);

        return ResponseEntity.ok().build();
    }
}