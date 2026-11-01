package by.Egrius.demoTask.entity.embeddable;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.time.LocalTime;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArrivalTime {

    @Column(name = "check_in", nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkIn;

    @Column(name = "check_out")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOut;
}