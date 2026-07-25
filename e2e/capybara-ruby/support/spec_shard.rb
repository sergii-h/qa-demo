# frozen_string_literal: true

module SpecShard
  module_function

  def spec_files(suite: 'e2e')
    root = File.expand_path('..', __dir__)
    files = Dir[File.join(root, 'spec/**/*_spec.rb')].sort

    case suite
    when 'uat'
      files.select { |file| file.end_with?('_uat_spec.rb') }
    when 'accessibility'
      files.select { |file| file.end_with?('_axe_spec.rb') }
    when 'all'
      files
    else
      files.reject { |file| file.end_with?('_uat_spec.rb', '_axe_spec.rb') }
    end
  end

  def files_for_shard(suite:, split_count:, split_index:)
    files = spec_files(suite: suite)
    return files if split_count <= 1

    files.select.with_index { |_, index| index % split_count == split_index }
  end
end
