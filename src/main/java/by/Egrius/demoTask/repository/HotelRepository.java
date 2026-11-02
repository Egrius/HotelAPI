package by.Egrius.demoTask.repository;

import by.Egrius.demoTask.entity.Hotel;
import by.Egrius.demoTask.repository.projection.AmenityCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

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
