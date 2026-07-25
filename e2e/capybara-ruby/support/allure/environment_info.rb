# frozen_string_literal: true

require 'fileutils'
require 'rbconfig'
require 'socket'

require_relative '../../data/devices'
require_relative '../test_config'

module AllureEnvironmentInfo
  module_function

  def build(browser: {}, device: {})
    {
      'Framework' => 'Capybara + RSpec',
      'os_release' => RbConfig::CONFIG['host_os'],
      'ruby_version' => RUBY_VERSION,
      'environment' => TestConfig.base_url,
      'device' => device[:label] || 'desktop',
      'browser' => browser[:name] || 'chrome',
      'browser_version' => browser[:version] || chrome_version
    }
  end

  def write(results_dir, browser: {}, device: {})
    lines = build(browser: browser, device: device).map { |key, value| "#{key}=#{value}" }.join("\n")
    FileUtils.mkdir_p(results_dir)
    File.write(File.join(results_dir, 'environment.properties'), "#{lines}\n")
  end

  def chrome_version
    version_from_selenium || version_from_cli || 'unknown'
  end

  def version_from_selenium
    options = Selenium::WebDriver::Chrome::Options.new
    options.add_argument('--headless=new')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-gpu')
    driver = Selenium::WebDriver.for(:chrome, options: options)
    driver.capabilities.browser_version
  rescue StandardError
    nil
  ensure
    driver&.quit
  end

  def version_from_cli
    binaries = [
      'google-chrome',
      'google-chrome-stable',
      'chromium',
      'chromium-browser',
      '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'
    ]

    binaries.each do |binary|
      output = `"#{binary}" --version 2>/dev/null`.to_s.strip
      next if output.empty?

      match = output.match(/\d+\.\d+\.\d+\.\d+/) || output.match(/\d+\.\d+\.\d+/)
      return match[0] if match
    end

    nil
  end

  def resolve_device_label_from_dir(source_dir)
    if source_dir.include?('.device-desktop')
      device = Devices::DESKTOP
      return "#{device[:label]} (#{device[:viewport_width]}x#{device[:viewport_height]})"
    end

    if source_dir.include?('.device-mobile')
      device = Devices::MOBILE
      return "#{device[:label]} (#{device[:viewport_width]}x#{device[:viewport_height]})"
    end

    nil
  end

  def write_merged(target_dir, source_dirs)
    browser = 'chrome'
    browser_version = 'unknown'

    source_dirs.each do |source_dir|
      env_file = File.join(source_dir, 'environment.properties')
      next unless File.exist?(env_file)

      content = File.read(env_file)
      browser = content[/^browser=(.+)$/, 1] || browser
      browser_version = content[/^browser_version=(.+)$/, 1] || browser_version
      break
    end

    device = source_dirs.filter_map { |dir| resolve_device_label_from_dir(dir) }.join('; ')
    device = Devices::DESKTOP[:label] if device.empty?

    write(target_dir, browser: { name: browser, version: browser_version }, device: { label: device })
  end
end
