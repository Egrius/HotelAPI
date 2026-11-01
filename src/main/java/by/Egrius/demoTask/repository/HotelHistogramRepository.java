package by.Egrius.demoTask.repository;

import java.util.Map;

public interface HotelHistogramRepository {
    Map<String, Long> getHistogram(String param);
}
