# frozen_string_literal: true

require_relative '../providers/step_provider'
require_relative '../providers/support_provider'
require_relative '../providers/validation_provider'

module Providers
  module_function

  def step
    @step ||= StepProvider.new
  end

  def validate
    @validate ||= ValidationProvider.new
  end

  def support
    @support ||= SupportProvider.new
  end

  def reset!
    @step = nil
    @validate = nil
    @support = nil
  end
end

step = Providers.method(:step)
validate = Providers.method(:validate)
support = Providers.method(:support)
