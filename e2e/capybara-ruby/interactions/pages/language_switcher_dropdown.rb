# frozen_string_literal: true

require_relative '../../support/capybara_helpers'

class LanguageSwitcherDropdown
  include CapybaraHelpers
  def dropdown
    find('[data-testid="language-switcher"]')
  end

  def dropdown_value
    dropdown.find('span.p-dropdown-label')
  end

  def items
    find_all('.p-dropdown-item')
  end
end
