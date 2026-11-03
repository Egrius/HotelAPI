package by.Egrius.demoTask.controller;

import by.Egrius.demoTask.dto.hotel.HotelCreateDto;
import by.Egrius.demoTask.dto.hotel.HotelReadDto;
import by.Egrius.demoTask.dto.hotel.HotelShortInfoReadDto;
import by.Egrius.demoTask.repository.filter.HotelFilter;
import by.Egrius.demoTask.service.HotelService;
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
public class ApiController {

    private final HotelService hotelService;

    // имело бы смысл добавить пагинацию
    // 1) Короткая информация о всех отелях
    @GetMapping("/hotels")
    public List<HotelShortInfoReadDto> getHotelsShortInfo() {
        return hotelService.getAllHotelsShortInfo();
    }

    // 2) Расширенная информация по конкретному отелю
    @GetMapping("/hotels/{id}")
    public HotelReadDto getHotelsInfo(@PathVariable("id") Long id) {
        return hotelService.getHotelById(id);
    }

    /*
    3) поиск получение списка всех отелей с их КРАТКОЙ информацией по следующим параметрам: name, brand, city, country, amenities.
	Например: /search?city=minsk
     */
    @GetMapping("/search")
    public List<HotelShortInfoReadDto> getHotelsShortInfoByFilter(@RequestParam(value = "name", required = false) String name,
                                                             @RequestParam(value = "brand", required = false) String brand,
                                                             @RequestParam(value = "city", required = false) String city,
                                                             @RequestParam(value = "country", required = false) String country,
                                                             @RequestParam(value = "amenities", required = false) List<String> amenities) {

        HotelFilter filter = new HotelFilter(name, brand, city, country, amenities);

        return hotelService.getHotelsByFilter(filter);
    }

    /*
    6) получение колличества отелей сгруппированных по каждому значению указанного параметра.
    Параметр: brand, city, country, amenities
     */
    @GetMapping("/histogram/{param}")
    public Map<String, Long> getHistogramByParameter(@PathVariable("param") String param) {
        return hotelService.getHistogramByParam(param);
    }

    // 4) создание нового отеля
    @PostMapping("/hotels")
    public HotelShortInfoReadDto createHotel(
            @Valid @RequestBody HotelCreateDto hotelCreateDto) {

        return hotelService.createHotel(hotelCreateDto);
    }

    // 5) добавление списка amenities к отелю
    @PostMapping(value = "/hotels/{id}/amenities", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addHotelAmenities(
            @PathVariable("id") Long id,
            @Valid @NotEmpty @RequestBody List<@NotBlank @Size(max = 255) String> amenities) {

        hotelService.addAmenities(id, amenities);

        return ResponseEntity.ok().build();
    }
}