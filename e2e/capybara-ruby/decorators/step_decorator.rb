# frozen_string_literal: true

require 'json'

module StepDecorator
  module Methods
    def step(step_name)
      @pending_step_name = step_name
    end

    def method_added(method_name)
      return if @wrapping_step
      return unless @pending_step_name

      step_name = @pending_step_name
      @pending_step_name = nil
      @wrapping_step = true

      original = instance_method(method_name)
      define_method(method_name) do |*args, **kwargs, &block|
        resolved_name = StepDecorator.replace_placeholders(step_name.dup, args, kwargs)
        result = nil
        Allure.run_step(resolved_name) do
          result = original.bind_call(self, *args, **kwargs, &block)
        end
        result
      end
    ensure
      @wrapping_step = false
    end
  end

  module_function

  def replace_placeholders(template, args, kwargs = {})
    values = args + kwargs.values
    values.each do |value|
      break unless template.match?(/({.*?})/)

      replacement =
        case value
        when String, Numeric then value.to_s
        when Array then format_array(value)
        when Hash then format_object(value)
        else value.to_s
        end

      template = template.sub(/({.*?})/, replacement)
    end
    template
  end

  def format_array(array)
    "[#{array.map { |item| format_value(item) }.join(', ')}]"
  end

  def format_object(object)
    seen = {}

    format = lambda do |value|
      return format_value(value) unless value.is_a?(Hash) || value.is_a?(Array)
      return '[Circular]' if seen[value]

      seen[value] = true
      if value.is_a?(Array)
        format_array(value)
      else
        entries = value.map { |key, val| "#{key}: #{format.call(val)}" }.join(', ')
        "{#{entries}}"
      end
    end

    format.call(object)
  end

  def format_value(value)
    case value
    when Array then format_array(value)
    when Hash then format_object(value)
    else value.to_s
    end
  end
end
