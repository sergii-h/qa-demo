# frozen_string_literal: true

require_relative '../../support/capybara_helpers'

class TaskInfoModal
  include CapybaraHelpers
  def title
    find('[data-testid="modal-title"]')
  end

  def description
    find('[data-testid="description"]')
  end

  def validation_badge
    find('[data-testid="valid"]')
  end

  def status_tag(status)
    find('[data-testid="status"]').find("[data-testid=\"status-tag-#{status}\"]")
  end

  def priority_tag(priority)
    find('[data-testid="priority"]').find("[data-testid=\"priority-tag-#{priority}\"]")
  end
end
