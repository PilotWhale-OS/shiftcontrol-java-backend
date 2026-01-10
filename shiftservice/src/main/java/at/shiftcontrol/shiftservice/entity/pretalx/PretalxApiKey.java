package at.shiftcontrol.shiftservice.entity.pretalx;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
        return new ToStringBuilder(this)
            .append("apiKey", apiKey)
            .toString();
    }
}
