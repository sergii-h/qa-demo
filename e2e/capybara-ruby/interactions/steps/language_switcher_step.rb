# frozen_string_literal: true

require_relative '../../decorators/step_decorator'
require_relative '../../support/capybara_helpers'
require_relative '../pages/language_switcher_dropdown'

class LanguageSwitcherStep
  extend StepDecorator::Methods
  include CapybaraHelpers

  def initialize
    @dropdown = LanguageSwitcherDropdown.new
  end

  step "Select language '{language}'"
  def select_language(language)
    @dropdown.dropdown.click
    @dropdown.items.find { |item| item.text == language }.click
    expect(@dropdown.dropdown_value).to have_text(language)
  end
end
