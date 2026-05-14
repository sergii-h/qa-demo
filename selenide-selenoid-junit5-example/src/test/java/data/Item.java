package data;

import lombok.Builder;
import lombok.Data;

import static org.apache.commons.lang3.RandomStringUtils.secure;

@Builder
@Data
public class Item {
    @Builder.Default
    private String name = secure().nextAlphabetic(12);

    @Builder.Default
    private String amount = "1";

    @Builder.Default
    private String description = secure().nextAlphabetic(12);
}
