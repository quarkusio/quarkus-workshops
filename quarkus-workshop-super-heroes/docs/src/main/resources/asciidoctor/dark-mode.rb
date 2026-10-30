# Dark-mode copies of diagrams and images. Each gets a light and a dark copy, tagged light-only and
# dark-only, and dark.css shows the one matching the reader's colour scheme.
#
# Diagrams: renders every PlantUML diagram twice, once in its usual theme and once in the dark
# variant of that theme, tagged as above.
#
# The dark theme is the light one with -dark appended to its name, so
# theme="quarkus from https://..." becomes theme="quarkus-dark from https://...". Diagrams with no
# theme at all use the plantuml-dark-theme document attribute, and keep a single light copy if it
# is not set.
#
# Salt diagrams (the file trees) are the exception. PlantUML draws salt text in black whatever the
# theme or style says, so a dark render is unreadable. They are monochrome, though, so they get no
# dark copy, just an invert-in-dark role that dark.css turns into a colour inversion.
#
# Images: an image x.png that has an x-dark.png beside it in dark-images-dir gets the dark one as
# its dark copy. Content keeps referring to x.png only.
#
# This file goes through Maven resource filtering, so it must not contain an at sign.

module QuarkusWorkshop
  module DarkDiagrams
    def process(parent, reader_or_target, attributes)
      light = super(parent, reader_or_target, attributes.dup)
      if light.context == :image && salt?(parent, reader_or_target)
        light.add_role 'invert-in-dark'
        return light
      end
      dark_theme = dark_theme_for(parent, attributes)
      # A text-format diagram, or one that failed to render, comes back as something other than an
      # image; there is nothing to swap.
      return light unless dark_theme && light.context == :image

      dark_attributes = attributes.dup
      dark_attributes['theme'] = dark_theme
      # The anchor and the figure caption stay with the light copy. Repeating them would duplicate
      # the id and number the figure twice.
      %w(id title caption).each { |name| dark_attributes.delete(name) }
      dark_target = dark_image_name(parent, reader_or_target, attributes)
      # Named diagrams take their file name from the .puml, so without a target of its own the
      # dark render would overwrite the light one. Inline ones are named by a checksum that
      # already covers the theme.
      dark_attributes['target'] = dark_target if dark_target

      dark = super(parent, reader_or_target, dark_attributes)
      return light unless dark.context == :image

      # Otherwise the alt text is made from the -dark file name.
      dark.set_attr 'alt', light.attr('alt')
      dark.title = light.title if light.title?
      dark.caption = light.caption
      light.add_role 'light-only'
      dark.add_role 'dark-only'

      wrapper = create_open_block parent, nil, { 'role' => 'themed-diagram' }
      wrapper << light
      wrapper << dark
      wrapper
    end

    private

    def dark_theme_for(parent, attributes)
      theme = attributes['theme'] || parent.document.attr('plantuml-theme')
      if theme
        name, rest = theme.split(' ', 2)
        return nil if name.end_with? '-dark'
        [name + '-dark', rest].compact.join(' ')
      else
        parent.document.attr('plantuml-dark-theme')
      end
    end

    def salt?(parent, reader_or_target)
      return true if is_a?(Asciidoctor::Diagram::SaltBlockProcessor) || is_a?(Asciidoctor::Diagram::SaltBlockMacroProcessor)
      lines = if reader_or_target.is_a? String
                File.readlines(parent.normalize_system_path(parent.sub_attributes(reader_or_target)))
              else
                reader_or_target.lines
              end
      # "\x40" is an at sign, spelt out to keep it away from resource filtering.
      lines.any? { |line| line.strip.start_with? "\x40startsalt" }
    end

    def dark_image_name(parent, reader_or_target, attributes)
      if attributes['target']
        attributes['target'] + '-dark'
      elsif reader_or_target.is_a? String
        File.basename(parent.sub_attributes(reader_or_target), '.*') + '-dark'
      end
    end
  end

  class DarkImages < Asciidoctor::Extensions::Treeprocessor
    # Diagrams already come in pairs, and the salt ones are inverted instead.
    HANDLED_ROLES = %w(light-only dark-only invert-in-dark)

    def process(document)
      dir = document.attr('dark-images-dir')
      return unless dir

      # Collected first, because the loop below adds image blocks of its own.
      lights = document.find_by(context: :image) { |image| HANDLED_ROLES.none? { |role| image.has_role? role } }
      lights.each do |light|
        target = light.attr('target')
        dark_target = target.sub(/(\.\w+)\z/, '-dark\\1')
        next if dark_target == target || !File.exist?(File.join(dir, dark_target))

        attributes = light.attributes.merge('target' => dark_target)
        # The anchor stays with the light copy.
        attributes.delete('id')
        dark = Asciidoctor::Block.new(light.parent, :image, content_model: :empty, attributes: attributes)
        dark.title = light.title if light.title?
        dark.caption = light.caption
        light.add_role 'light-only'
        dark.add_role 'dark-only'
        siblings = light.parent.blocks
        siblings.insert(siblings.index(light) + 1, dark)
      end
      nil
    end
  end
end

Asciidoctor::Extensions.register do
  tree_processor QuarkusWorkshop::DarkImages
end

[
  Asciidoctor::Diagram::PlantUmlBlockProcessor,
  Asciidoctor::Diagram::PlantUmlBlockMacroProcessor,
  Asciidoctor::Diagram::SaltBlockProcessor,
  Asciidoctor::Diagram::SaltBlockMacroProcessor
].each { |processor| processor.prepend QuarkusWorkshop::DarkDiagrams }
