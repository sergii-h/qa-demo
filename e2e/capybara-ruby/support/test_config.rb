# frozen_string_literal: true

require 'dotenv'

env_local = File.expand_path('../.env.e2e.local', __dir__)
env_default = File.expand_path('../.env.e2e', __dir__)
Dotenv.load(env_local, env_default)

module TestConfig
  module_function

  def env(key, fallback)
    value = ENV.fetch(key, nil)
    value.nil? || value.empty? ? fallback : value
  end

  def base_url
    env('E2E_TEST_ENV_URL', 'http://localhost:5173')
  end

  def wiremock_url
    env('E2E_WIREMOCK_URL', 'http://localhost:8085')
  end

  def api_url
    env('E2E_API_URL', 'http://localhost:8080/v1')
  end

  def mongodb_url
    env('E2E_MONGODB_URL', 'mongodb://localhost:27017')
  end
end
