package by.Egrius.demoTask.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contacts {

    @Column(name = "phone", nullable = false, length = 150)
    private String phone;

    @Column(name = "email", nullable = false, length = 150)
    private String email;
}