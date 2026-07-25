# frozen_string_literal: true

require_relative '../support/api/api_client'
require_relative '../support/mocks/api_route_mock'
require_relative '../support/mocks/wire_mock_client'

class SupportProvider
  attr_reader :api, :wiremock, :mock

  def initialize
    @api = ApiClient.new
    @wiremock = WireMockClient.new
    @mock = MockFacade.new
  end

  class MockFacade
    attr_reader :api

    def initialize
      @api = ApiRouteMock.new
    end
  end
end
