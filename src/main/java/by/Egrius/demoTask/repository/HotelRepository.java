package by.Egrius.demoTask.repository;

import by.Egrius.demoTask.entity.Hotel;
import by.Egrius.demoTask.repository.projection.AmenityCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query("SELECT a AS amenity, COUNT(h) AS count FROM Hotel h JOIN h.amenities a GROUP BY a ORDER BY a")
    List<AmenityCountProjection> getHistogramByHotelAmenity();
}
