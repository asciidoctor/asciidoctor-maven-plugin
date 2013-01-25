require 'asciidoctor'
require 'find'

Find.find($srcDir) do |path|
  if path =~ /.*\.a((sc(iidoc)?)|d(oc)?)$/
    Asciidoctor.render_file({path, :in_place => true, :safe => Asciidoctor::SafeMode::UNSAFE,
                             :base_dir => $srcDir, :backend => $backend})
  end
end
