# frozen_string_literal: true

require 'json'

require_relative '../../decorators/step_decorator'
require_relative '../../support/capybara_helpers'

class AccessibilityValidator
  extend StepDecorator::Methods
  include RSpec::Matchers

  step 'Validate no accessibility violations'
  def has_no_violations(results)
    Allure.add_attachment(
      name: 'Axe accessibility report',
      source: JSON.pretty_generate(results['violations']),
      type: Allure::ContentType::JSON
    )
    expect(results['violations']).to eq([])
  end
end
