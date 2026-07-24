# frozen_string_literal: true

require_relative '../../decorators/step_decorator'
require_relative '../../support/capybara_helpers'

class NavigationStep
  extend StepDecorator::Methods
  include CapybaraHelpers

  step 'Navigate to main page'
  def open_main_page
    visit('/')
  end

  step 'Refresh page'
  def refresh
    page.driver.refresh
  end
end
