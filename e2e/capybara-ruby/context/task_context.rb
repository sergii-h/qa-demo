# frozen_string_literal: true

require 'securerandom'

require_relative '../data/task_priority'
require_relative '../data/task_status'

class TaskContext
  attr_reader :id, :title, :description, :status, :priority

  def initialize(overrides = {})
    @id = overrides[:id] || SecureRandom.uuid
    @title = overrides[:title] || "Task-#{SecureRandom.uuid.split('-').first}"
    @description = overrides[:description] || 'Automated test task description'
    @status = overrides[:status] || TaskStatus::TODO
    @priority = overrides[:priority] || TaskPriority::MEDIUM
  end

  def create_task_request
    {
      title: title,
      description: description,
      status: status,
      priority: priority
    }
  end

  def create_task_data
    {
      title: title,
      description: description,
      status: status,
      priority: priority
    }
  end

  def create_task_response
    {
      id: id,
      title: title,
      description: description,
      status: status,
      priority: priority
    }
  end
end
