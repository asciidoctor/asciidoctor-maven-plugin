require 'asciidoctor'

asciidoctor_opts = {:safe => Asciidoctor::SafeMode::UNSAFE, :base_dir => $srcDir}
Dir.new($srcDir).each do |file|
  file_ext = File.extname(file.to_s)
  if file_ext === '.adoc' or file_ext === '.asciidoc' or file_ext === '.asc' or file_ext === '.ad'
    basename = File.basename(file.to_s, file_ext) + ($backend === 'docbook' ? '.xml' : '.html')
    doc = Asciidoctor::Document.new(file.lines.to_a, asciidoctor_opts)
    File.new($outputDir + '/' + basename, 'w+').puts doc.render
  end
end
