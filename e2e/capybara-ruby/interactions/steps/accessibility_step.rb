# frozen_string_literal: true

require_relative '../../decorators/step_decorator'
require_relative '../../support/capybara_helpers'

class AccessibilityStep
  extend StepDecorator::Methods
  include CapybaraHelpers

  AXE_SOURCE = File.read(File.expand_path('../../vendor/axe.min.js', __dir__)).freeze

  def initialize
    @axe_injected = false
  end

  step 'Analyze page accessibility with axe'
  def analyze
    inject_axe
    page.driver.browser.execute_async_script(<<~JS)
      const callback = arguments[arguments.length - 1];
      axe.run(document, { runOnly: { type: 'tag', values: ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'] } })
        .then(callback);
    JS
  end

  private

  def inject_axe
    return if @axe_injected

    page.execute_script(AXE_SOURCE)
    @axe_injected = true
  end
end
