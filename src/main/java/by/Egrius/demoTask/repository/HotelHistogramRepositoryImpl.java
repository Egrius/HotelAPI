package by.Egrius.demoTask.repository;

import by.Egrius.demoTask.entity.Hotel;
import by.Egrius.demoTask.repository.projection.HistogramProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Repository
@RequiredArgsConstructor
public class HotelHistogramRepositoryImpl implements HotelHistogramRepository {

    private final EntityManager entityManager;

    private static final Map<String, Function<Root<Hotel>, Path<String>>> PATHS = Map.of(
            "brand",   root -> root.get("brand"),
            "city",    root -> root.get("address").get("city"),
            "country", root -> root.get("address").get("country")
    );


    @Override
    public Map<String, Long> getHistogram(String param) {

        if(param == null) throw new IllegalArgumentException("Param can't be null");

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<HistogramProjection> cq = cb.createQuery(HistogramProjection.class);
        Root<Hotel> root = cq.from(Hotel.class);

        Function<Root<Hotel>, Path<String>> pathFunction = PATHS.get(param);

        if (pathFunction == null) {
            throw new IllegalArgumentException(
                    "Unsupported param: '" + param + "'");
        }

        Path<String> path = pathFunction.apply(root);

        cq.select(cb.construct(HistogramProjection.class, path, cb.count(root)));
        cq.groupBy(path);
        cq.orderBy(cb.asc(path));

        return toMap(entityManager.createQuery(cq).getResultList());
    }

    private Map<String, Long> toMap(List<HistogramProjection> list) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (HistogramProjection e : list) {
            result.put(e.key(), e.count());
        }
        return result;
    }
}
