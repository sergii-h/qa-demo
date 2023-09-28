package data;

import lombok.Builder;
import lombok.Data;

@Builder(builderMethodName = "hiddenBuilder")
@Data
public class Environment {
    @Builder.Default
    public String id = "id";

    @Builder.Default
    public boolean isLocal = false;

    @Builder.Default
    public String db = "db connection string";

    public String url;
    public String beUrl;
    public String remoteWebDriverUrl;
    public String externalUrl;
    public String browserInfo;

    public static EnvironmentBuilder builder(String envName) {
        return switch (envName) {
            case "local" -> hiddenBuilder()
                    .id(envName)
                    .isLocal(true)
                    .url("http://localhost:3000")
                    .beUrl("http://localhost:8080/v1");

            case "docker" -> hiddenBuilder()
                    .id(envName)
                    .isLocal(false)
                    .url("http://host.docker.internal:3000")
                    .beUrl("http://host.docker.internal:8080/v1")
                    .remoteWebDriverUrl("http://host.docker.internal:4444/wd/hub");

            case "remote" -> hiddenBuilder()
                    .id(envName)
                    .isLocal(false)
                    .url("http://host.docker.internal:3000")
                    .beUrl("http://localhost:8080/v1")
                    .remoteWebDriverUrl("http://localhost:4444/wd/hub");

            default -> hiddenBuilder()
                        .id(envName)
                        .isLocal(false);
        };
    }
}
