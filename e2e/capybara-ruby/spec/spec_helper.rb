# frozen_string_literal: true

require 'fileutils'

root = File.expand_path('..', __dir__)
$LOAD_PATH.unshift(root)

require 'dotenv'
env_local = File.join(root, '.env.e2e.local')
env_default = File.join(root, '.env.e2e')
Dotenv.load(env_local, env_default)

require 'capybara/rspec'
require 'allure-rspec'
require 'selenium-webdriver'

require_relative '../support/test_config'
require_relative '../data/devices'
require_relative '../support/allure/environment_info'
require_relative '../fixtures/providers'

AllureRspec.configure do |config|
  config.results_directory = ENV.fetch('ALLURE_RESULTS_DIR', File.join(root, 'allure-results'))
  config.clean_results_directory = false
  config.logging_level = Logger::WARN
  config.link_tms_pattern = 'https://github.com/sergii-h/qa-demo/issues/{}'
  config.link_issue_pattern = 'https://github.com/sergii-h/qa-demo/issues/{}'
end

Capybara.server = :puma, { Silent: true }
Capybara.app_host = TestConfig.base_url
Capybara.default_max_wait_time = 10

suite = ENV.fetch('CAPYBARA_SUITE', 'e2e')
device_name = ENV.fetch('CAPYBARA_DEVICE', 'desktop')
device = Devices.resolve(device_name)
use_billy = suite != 'uat'

chrome_options = lambda do
  Selenium::WebDriver::Chrome::Options.new.tap do |options|
    options.add_argument('--headless=new')
    options.add_argument('--disable-gpu')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')
    options.add_argument('--disable-background-networking')
    options.add_argument('--disable-component-update')
    options.add_argument('--disable-default-apps')
    options.add_argument('--disable-sync')
    options.add_argument('--no-first-run')
    options.add_argument('--window-size=1280,720')
  end
end

if use_billy
  require 'billy/capybara/rspec'

  Billy.configure do |config|
    config.cache_path = File.join(root, 'tmp', 'billy-cache')
    config.logger = Logger.new(File::NULL)
  end

  Capybara.register_driver :selenium_chrome_headless_billy do |app|
    Capybara::Selenium::Driver.new(
      app,
      browser: :chrome,
      options: chrome_options.call.tap do |options|
        options.add_argument('--enable-features=NetworkService,NetworkServiceInProcess')
        options.add_argument('--ignore-certificate-errors')
        options.add_argument("--proxy-server=#{Billy.proxy.host}:#{Billy.proxy.port}")
        options.add_argument('--proxy-bypass-list=<-loopback>')
      end,
      clear_local_storage: true,
      clear_session_storage: true
    )
  end
end
Capybara.register_driver :selenium_chrome_headless do |app|
  Capybara::Selenium::Driver.new(app, browser: :chrome, options: chrome_options.call)
end

driver_name = use_billy ? :selenium_chrome_headless_billy : :selenium_chrome_headless
Capybara.javascript_driver = driver_name
Capybara.default_driver = driver_name

RSpec.configure do |config|
  config.add_formatter AllureRspec::RSpecFormatter

  config.expect_with :rspec do |expectations|
    expectations.include_chain_clauses_in_custom_matcher_descriptions = true
  end

  config.mock_with :rspec do |mocks|
    mocks.verify_partial_doubles = true
  end

  config.shared_context_metadata_behavior = :apply_to_host_groups
  config.filter_run_when_matching :focus
  config.example_status_persistence_file_path = '.rspec_status'
  config.disable_monkey_patching!
  config.order = :defined

  config.before(:suite) do
    FileUtils.mkdir_p(AllureRspec.configuration.results_directory)
    AllureEnvironmentInfo.write(
      AllureRspec.configuration.results_directory,
      browser: { name: 'chrome (headless)', version: AllureEnvironmentInfo.chrome_version },
      device: device
    )
  end

  config.before(:each, type: :system) do |example|
    browser = Capybara.current_session.driver.browser
    browser.execute_cdp(
      'Emulation.setDeviceMetricsOverride',
      width: device[:viewport_width],
      height: device[:viewport_height],
      deviceScaleFactor: 1,
      mobile: !device[:user_agent].nil?
    )
    if device[:user_agent]
      browser.execute_cdp(
        'Network.setUserAgentOverride',
        userAgent: device[:user_agent]
      )
    end

    Allure.parameter('Device', device[:label])
    Allure.label('parentSuite', device[:label])
    Allure.lifecycle.update_test_case do |test_case|
      test_case.name = "[#{device_name}] #{example.description}"
      test_case.full_name = "#{example.full_description} [#{device_name}]"
    end

    if use_billy
      Billy.proxy.reset_cache
      Billy.proxy.reset
    end
    Providers.reset!
  end

  config.after(:each, type: :system) do |example|
    next unless example.exception

    screenshot_dir = File.join(root, 'screenshots')
    FileUtils.mkdir_p(screenshot_dir)
    screenshot_path = File.join(
      screenshot_dir,
      "#{example.full_description.gsub(/[^a-zA-Z0-9]+/, '_').downcase}.png"
    )
    page.save_screenshot(screenshot_path)
    Allure.add_attachment(name: 'Screenshot', source: File.open(screenshot_path), type: Allure::ContentType::PNG)
  end

  case suite
  when 'uat'
    config.filter_run_including uat: true
  when 'accessibility'
    config.filter_run_including accessibility: true
  when 'all'
    # no filter
  else
    config.filter_run_excluding uat: true, accessibility: true
  end
end

def step = Providers.step

def validate = Providers.validate

def support = Providers.support
