# frozen_string_literal: true

require 'faraday'

require_relative '../test_config'

class ApiClient
  def initialize
    @base_url = TestConfig.api_url
    @connection = Faraday.new(url: @base_url) do |faraday|
      faraday.request :json
      faraday.response :json
      faraday.adapter Faraday.default_adapter
    end
  end

  def create_task(task)
    response = @connection.post('tasks', task)
    response.body
  end

  def delete_task(id)
    @connection.delete("tasks/#{id}")
  end

  def get_tasks
    response = @connection.get('tasks')
    response.body
  end
end
