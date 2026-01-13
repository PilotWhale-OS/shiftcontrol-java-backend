package at.shiftcontrol.lib.entity;

import java.util.StringJoiner;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class PretalxApiKey {
    @Id
    private String apiKey;

    @Override
    public String toString() {
        return new StringJoiner(", ", PretalxApiKey.class.getSimpleName() + "[", "]")
            .add("apiKey='" + apiKey + "'")
            .toString();
    }
}
