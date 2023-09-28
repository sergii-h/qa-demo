package data;

import lombok.Builder;
import lombok.Data;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

@Builder
@Data
public class Item {
    @Builder.Default
    public String name = randomAlphabetic(12);

    @Builder.Default
    public String amount = "1";

    @Builder.Default
    public String description = randomAlphabetic(12);
}
