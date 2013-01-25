require 'asciidoctor'
require 'asciidoctor/cli/invoker'
require 'asciidoctor/cli/options'
require 'find'

Find.find($srcDir) do |path|
  if path =~ /.*\.a((sc(iidoc)?)|d(oc)?)$/
    Asciidoctor::Cli::Invoke.new({:safe => Asciidoctor::SafeMode::UNSAFE, :base_dir => $srcDir, :backend => $backend}).invoke!
  end
end
