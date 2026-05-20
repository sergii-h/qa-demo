package provider;

import lombok.Getter;
import support.api.ApiClient;
import support.mock.WireMockClient;

@Getter
public class SupportProvider {
    public ApiClient api = new ApiClient();
    public WireMockClient wiremock = new WireMockClient();
}
