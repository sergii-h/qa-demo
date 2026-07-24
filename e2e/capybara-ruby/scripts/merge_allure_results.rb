#!/usr/bin/env ruby
# frozen_string_literal: true

source_dirs = ARGV[1..] || []
target_dir = File.expand_path(ARGV[0] || 'allure-results', __dir__ + '/..')

if source_dirs.empty?
  exit 0
end

require 'fileutils'
require_relative '../support/allure/environment_info'

FileUtils.mkdir_p(target_dir)
Dir.children(target_dir).each do |entry|
  path = File.join(target_dir, entry)
  FileUtils.rm_f(path) if File.file?(path)
end

absolute_sources = source_dirs.map { |dir| File.expand_path(dir, __dir__ + '/..') }

absolute_sources.each do |source_dir|
  next unless Dir.exist?(source_dir)

  Dir.children(source_dir).each do |file|
    source_path = File.join(source_dir, file)
    next unless File.file?(source_path)

    target_path = File.join(target_dir, file)
    FileUtils.cp(source_path, target_path) unless File.exist?(target_path)
  end
end

AllureEnvironmentInfo.write_merged(target_dir, absolute_sources) if absolute_sources.size > 1

absolute_sources.each do |source_dir|
  FileUtils.rm_rf(source_dir)
end
