# frozen_string_literal: true

class ApiRouteMock
  JSON_HEADERS = {
    'Content-Type' => 'application/json',
    'Access-Control-Allow-Origin' => '*'
  }.freeze

  CORS_HEADERS = {
    'Access-Control-Allow-Origin' => '*',
    'Access-Control-Allow-Methods' => 'GET, POST, PUT, DELETE, OPTIONS',
    'Access-Control-Allow-Headers' => 'Content-Type'
  }.freeze

  def create_task(response)
    stub_request(:post, %r{/v1/tasks$}, json: response, code: 201)
    self
  end

  def get_tasks(response)
    stub_request(:get, %r{/v1/tasks$}, json: response, code: 200)
    self
  end

  def get_task(id, response)
    stub_request(:get, %r{/v1/tasks/#{Regexp.escape(id)}$}, json: response, code: 200)
    self
  end

  def delete_task(id)
    # Empty 204 body (JSON body can break Chromium fetch). Dup headers — Billy mutates them.
    Billy.proxy.stub(%r{/v1/tasks/#{Regexp.escape(id)}$}, method: 'DELETE').and_return(
      code: 204,
      body: '',
      headers: CORS_HEADERS.dup
    )
    self
  end

  def update_task(id, response)
    stub_request(:put, %r{/v1/tasks/#{Regexp.escape(id)}$}, json: response, code: 200)
    self
  end

  def get_is_valid(id, is_valid)
    stub_request(:get, %r{/v1/tasks/isValid/#{Regexp.escape(id)}$}, json: is_valid, code: 200)
    self
  end

  private

  def stub_request(method, url_pattern, json:, code:)
    Billy.proxy.stub(url_pattern, method: method.to_s.upcase).and_return(
      json: json,
      code: code,
      headers: JSON_HEADERS
    )
  end
end
