# frozen_string_literal: true

require_relative '../../support/capybara_helpers'

class MainPage
  include CapybaraHelpers
  def task_rows
    find_all('[data-testid^="task-title-"]')
  end

  def create_task_button
    find('[data-testid="add-task-button"]')
  end

  def language_switcher
    find('[data-testid="language-switcher"]')
  end

  def table_headers
    find('.p-datatable-thead')
  end

  def task_table_body
    find('.p-datatable-tbody')
  end

  def task_row_by_title(title)
    find('[data-testid^="task-title-"]', text: title)
  end

  def task_info_button_by_id(id)
    find("[data-testid=\"info-button-#{id}\"]")
  end

  def task_edit_button_by_id(id)
    find("[data-testid=\"edit-button-#{id}\"]")
  end

  def task_delete_button_by_id(id)
    find("[data-testid=\"delete-button-#{id}\"]")
  end

  def task_title_by_title(title)
    find('[data-testid^="task-title-"]', text: title)
  end

  def status_tag(status)
    first("[data-testid=\"status-tag-#{status}\"]")
  end

  def priority_tag(priority)
    first("[data-testid=\"priority-tag-#{priority}\"]")
  end
end
