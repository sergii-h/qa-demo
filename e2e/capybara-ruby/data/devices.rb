# frozen_string_literal: true

module Devices
  DESKTOP = {
    label: 'Desktop Chrome',
    viewport_width: 1280,
    viewport_height: 720
  }.freeze

  MOBILE = {
    label: 'Mobile (iPhone 12 Pro viewport)',
    viewport_width: 390,
    viewport_height: 844,
    user_agent: 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) ' \
                'AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1'
  }.freeze

  ALL = {
    'desktop' => DESKTOP,
    'mobile' => MOBILE
  }.freeze

  module_function

  def resolve(device_name = 'desktop')
    ALL.fetch(device_name, DESKTOP)
  end
end
