# frozen_string_literal: true

require 'faraday'

require_relative '../test_config'

class WireMockClient
  def initialize
    @base_url = TestConfig.wiremock_url
    @connection = Faraday.new(url: @base_url) do |faraday|
      faraday.request :json
      faraday.response :json
      faraday.adapter Faraday.default_adapter
    end
  end

  def clear_mocks
    @connection.delete('__admin/mappings')
    self
  end

  def stub_task_validation(is_valid)
    @connection.post('__admin/mappings', {
      request: {
        method: 'GET',
        urlPattern: '/api/tasks/.*/validation'
      },
      response: {
        status: 200,
        jsonBody: { valid: is_valid }
      }
    })
    self
  end
end
