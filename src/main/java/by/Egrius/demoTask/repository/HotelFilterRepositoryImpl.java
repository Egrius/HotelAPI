package by.Egrius.demoTask.repository;

import by.Egrius.demoTask.entity.Hotel;
import by.Egrius.demoTask.repository.filter.HotelFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class HotelFilterRepositoryImpl implements HotelFilterRepository {

    private final EntityManager entityManager;

    @Override
    public List<Hotel> findAllByFilter(HotelFilter filter) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Hotel> cq = cb.createQuery(Hotel.class);

        Root<Hotel> hotel = cq.from(Hotel.class);

        List<Predicate> filterPredicates = new ArrayList<>();

        if(filter.name() != null) {
            filterPredicates.add(cb.like(
                    cb.lower(hotel.get("name")),
                    "%" + filter.name().toLowerCase() + "%"));
        }

        if(filter.brand() != null) {
            filterPredicates.add(cb.equal(cb.lower(hotel.get("brand")), filter.brand().toLowerCase()));
        }

        if(filter.city() != null) {
            filterPredicates.add(cb.equal(cb.lower(hotel.get("address").get("city")), filter.city().toLowerCase()));
        }

        if(filter.country() != null) {
            filterPredicates.add(cb.equal(cb.lower(hotel.get("address").get("country")), filter.country().toLowerCase()));
        }

        if(filter.amenities() != null && !filter.amenities().isEmpty()) {
            Set<String> required = filter.amenities().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            Subquery<Long> subquery = cq.subquery(Long.class);
            Root<Hotel> subRoot = subquery.correlate(hotel);
            Join<Hotel, String> subAmenities = subRoot.join("amenities");

            subquery.select(subRoot.get("id"))
                    .where(cb.lower(subAmenities).in(required))
                    .groupBy(subRoot.get("id"))
                    .having(cb.equal(cb.count(subAmenities), (long) required.size()));

            filterPredicates.add(cb.exists(subquery));
        }

        cq.distinct(true);
        cq.where(filterPredicates.toArray(Predicate[]::new));
        cq.orderBy(cb.asc(hotel.get("id")));

        TypedQuery<Hotel> query = entityManager.createQuery(cq);

        return query.getResultList();
    }
}
