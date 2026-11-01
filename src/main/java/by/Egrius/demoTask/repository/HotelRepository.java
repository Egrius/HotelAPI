package by.Egrius.demoTask.repository;

import by.Egrius.demoTask.entity.Hotel;
import by.Egrius.demoTask.repository.filter.HotelFilter;
import by.Egrius.demoTask.repository.projection.AmenityCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    @Query(value = """
        SELECT
            :param,
            COUNT(*) as count
    FROM hotels
    GROUP BY :param
    ORDER BY :param
    """,
            nativeQuery = true)
    Map<String, Long> getHistogramByHotelParam(@Param("param") String param);

    @Query(value = """
        SELECT 
            ha.amenity AS amenity,
            COUNT(*) AS count
        FROM hotel_amenities ha
        GROUP BY ha.amenity
        ORDER BY ha.amenity
    """,
            nativeQuery = true)
    List<AmenityCountProjection> getHistogramByHotelAmenity();
}
