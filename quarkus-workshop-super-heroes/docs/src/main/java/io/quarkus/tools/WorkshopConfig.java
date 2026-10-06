package io.quarkus.tools;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.quarkiverse.roq.frontmatter.runtime.config.ConfiguredCollection;
import io.quarkus.runtime.annotations.ConfigDocDefault;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import io.smallrye.config.WithParentName;
import jakarta.validation.constraints.Pattern;

public class WorkshopConfig {


    @ConfigMapping(prefix = "workshop")
    @ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
    public interface RoqSiteConfig {
        String DIR_NAME_PATTERN = "^[a-zA-Z0-9._-]{1,255}$";
        String CONTENT_DIR = "content";
        String STATIC_DIR = "static";
        String PUBLIC_DIR = "public";
        String IGNORED_FILES = "**/_**,_**";

        String DEFAULT_PAGE_LINK = "/:path:ext";
        String DEFAULT_COLLECTION_LINK = "/:collection/:slug/";

        List<ConfiguredCollection> DEFAULT_COLLECTIONS = List
            .of(new ConfiguredCollection("posts", false, false, false, "post", DEFAULT_COLLECTION_LINK,
                Optional.empty()));

        /**
         * the directory for assets
         */
        @WithDefault("HADEFAUTL")
        @WithName("assetsdir")
        Optional<String> urlOptional();

        /**
         * The order of the route which handles the templates.
         * <p>
         * By default, the route is executed before the default routes (static resources, etc.).
         *
         * @asciidoclet
         */
        @WithDefault("HADEFAUTL")
        @WithName("imagesdir")
        Optional<String> hurlOptional();

        /**
         * Add new ignored files to the default list.
         * <p>
         * The ignored files (relative to the site directory).
         *
         * <p>
         * Only the <code>content/</code>, <code>public/</code>, and <code>static/</code> directories are scanned.
         * </p>
         */
        Optional<List<String>> ignoredFiles();

        /**
         * The default ignored files (relative to the site directory) include:
         * <ul>
         * <li>All files or directories starting with an underscore (<code>_</code>)</li>
         * </ul>
         *
         * <p>
         * These patterns are additional to the scanner's own OS-level defaults
         * (e.g. <code>.DS_Store</code>, <code>Thumbs.db</code>, <code>*~</code>, <code>.class</code>).
         * </p>
         */
        @WithDefault(IGNORED_FILES)
        List<String> defaultIgnoredFiles();

        /**
         * Pages whose content should be escaped&mdash;
         * i.e., included in Qute rendering but not parsed for Qute expressions.
         *
         * <p>
         * This is based on the page's relative path from the content directory.
         * </p>
         *
         * <p>
         * This applies only to <em>pages</em> (not layouts or partials).
         * </p>
         *
         * <p>
         * Supports glob expressions.
         * </p>
         */
        Optional<List<String>> escapedPages();

        /**
         * The layout to use for normal html pages if not specified in FM.
         * When empty, the page will not use a layout when it doesn't specify it in FM.
         * <p>
         * Resolves local layout first, then theme layout as fallback.
         */
        @WithDefault("page")
        Optional<String> pageLayout();

        /**
         * The directory which contains content (pages and collections) in the Roq site directory.
         */
        @WithDefault(CONTENT_DIR)
        @Pattern(regexp = DIR_NAME_PATTERN)
        String contentDir();

        /**
         * The directory (dir name) which contains static files to be served (with 'static/' prefix).
         *
         * @deprecated Use publicDir instead (Use 'public/static/...' to reproduce the same behaviour)
         */
        @WithDefault(STATIC_DIR)
        @Pattern(regexp = DIR_NAME_PATTERN)
        String staticDir();

        /**
         * The directory which contains public static files to be served without processing (dir name)
         */
        @WithDefault(PUBLIC_DIR)
        @Pattern(regexp = DIR_NAME_PATTERN)
        String publicDir();

        /**
         * The path containing static images (in the public directory)
         */
        @WithDefault("images/")
        String imagesPath();

        /**
         * When enabled it will select all FrontMatter pages in Roq Generator
         */
        @WithDefault("true")
        boolean generator();

        /**
         * Show future documents
         */
        @WithDefault("false")
        boolean future();

        /**
         * The theme name. Used to resolve theme layouts when using `theme-layout:` in front matter.
         * With a theme, `layout: foo` resolves local first, then theme layout as fallback.
         */
        @WithDefault("roq-base")
        Optional<String> theme();

        /**
         * Show draft pages
         */
        @WithDefault("false")
        boolean draft();

        /**
         * Directory name used to mark collection documents as draft when frontmatter does not define attribute `draft`.
         * Frontmatter `draft` takes precedence over this directory-based fallback.
         */
        @WithDefault("drafts")
        @Pattern(regexp = DIR_NAME_PATTERN)
        String draftDirectory();

        /**
         * Format for dates
         */
        @WithDefault("yyyy-MM-dd[ HH:mm][:ss][ Z]")
        String dateFormat();

        /**
         * The default timezone
         */
        @ConfigDocDefault("document timezone if provided or system timezone")
        Optional<String> timeZone();

        default ZoneId timeZoneOrDefault() {
            return timeZone().isPresent() ? ZoneId.of(timeZone().get()):ZoneId.systemDefault();
        }

        /**
         * The default language to use when no language is specified in the frontmatter.
         * This language will be used as a fallback for articles that don't have a 'locale' property.
         */
        @WithDefault("en")
        String defaultLocale();

        /**
         * Indicates whether file names in the public directory and files attached to pages should be slugified
         * (converted to a URL-friendly format).
         * <p>
         * When enabled, file names will automatically be transformed into a URL-safe format.
         * Additionally, `page.file` and `site.file` references can use the original file names,
         * as they will also be slugified during the process.
         */
        @WithDefault("true")
        boolean slugifyFiles();

        /**
         * The directory names (in the Roq site directory) containing collections as key
         * and the corresponding collection config as value
         */
        @ConfigDocDefault("posts=true,posts.layout=post")
        @WithName("collections")
        Map<String, io.quarkiverse.roq.frontmatter.runtime.config.RoqSiteConfig.CollectionConfig> collectionsMap();

        /**
         * The directory where the generated templates should be created inside the output directory.
         */
        @WithDefault("roq-templates")
        @Pattern(regexp = DIR_NAME_PATTERN)
        String generatedTemplatesOutputDir();

        default List<ConfiguredCollection> collections() {
            return java.util.stream.Stream.concat(
                    collectionsMap().entrySet().stream()
                        .filter(e -> e.getValue().enabled())
                        .map(e -> new ConfiguredCollection(
                            e.getKey(),
                            false,
                            e.getValue().hidden(),
                            e.getValue().future(),
                            e.getValue().layout().orElse(null),
                            e.getValue().link(),
                            e.getValue().fromData()
                                .map(value -> new ConfiguredCollection.CollectionFromData(
                                    value.idKey(),
                                    value.name().orElse(e.getKey()))))),
                    DEFAULT_COLLECTIONS.stream()
                        .filter(dc -> !collectionsMap().containsKey(dc.id())))
                .toList();
        }

        /**
         * <strong>READ CAREFULLY:</strong><br>
         * The root path of your site (e.g. <code>/blog</code>) should be set using
         * <code>quarkus.http.root-path</code>.<br>
         * This path prefix should be relative to the Quarkus HTTP root path and is meant to be used
         * only when the Roq site is served alongside a Quarkus application on a separate path.
         */
        Optional<String> pathPrefix();

        default String pathPrefixOrEmpty() {
            return pathPrefix().orElse("");
        }

        /**
         * Default link template for non-collection pages.
         * Can be overridden per-page using the frontmatter {@code link} key.
         * Supports placeholders: {@code :path}, {@code :slug}, {@code :name}, {@code :ext}, etc.
         */
        @WithDefault(DEFAULT_PAGE_LINK)
        String pageLink();

        interface CollectionConfig {
            /**
             * If this collection is enabled
             */
            @WithParentName
            @WithDefault("true")
            boolean enabled();

            /**
             * Show future documents (overrides global future for this collection)
             */
            @WithDefault("false")
            boolean future();

            /**
             * If true, the collection won't be available on path but consumable as data.
             */
            @WithDefault("false")
            boolean hidden();

            /**
             * The layout to use if not specified in FM data.
             * When empty, the document will not use a layout when it doesn't specify it in FM.
             * <p>
             * Resolves local layout first, then theme layout as fallback.
             */
            Optional<String> layout();

            /**
             * Default link template for documents in this collection.
             * Can be overridden per-page using the frontmatter {@code link} key.
             * Supports placeholders: {@code :collection}, {@code :slug}, {@code :name}, {@code :ext}, etc.
             */
            @WithDefault(DEFAULT_COLLECTION_LINK)
            String link();

            /**
             * If present, documents for this collection will be created for the data (array or dir) with the same name as the
             * collection (using the collection default layout)
             */
            Optional<io.quarkiverse.roq.frontmatter.runtime.config.RoqSiteConfig.CollectionFromData> fromData();
        }

        interface CollectionFromData {

            /**
             * The data attribute to use as the page identifier. The value is slugified.
             */
            String idKey();

            /**
             * The name of the data source (file or directory in data/). Defaults to the collection id.
             */
            Optional<String> name();
        }
    }
}
