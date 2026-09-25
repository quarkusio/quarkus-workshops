package io.quarkus.tools;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import io.quarkus.qute.RawString;
import io.quarkus.qute.TemplateExtension;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@TemplateExtension
public class FiltersExtension {



    /**
     * Null-safe get for JsonObject. Prevents NPE when key is null (e.g. from ?? operator).
     */
    static Object get(JsonObject obj, String key) {
        if (obj==null || key==null || key.isEmpty()) {
            return null;
        }
        return obj.getValue(key);
    }

    /**
     * Jekyll's "where" filter: select items from an array where a property matches a value.
     * Usage in Qute: {myArray.where("key", "value")}
     */
    static JsonArray where(JsonArray array, String property, String value) {
        JsonArray result = new JsonArray();
        for (int i = 0; i < array.size(); i++) {
            Object item = array.getValue(i);
            if (item instanceof JsonObject obj) {
                Object propValue = obj.getValue(property);
                if (propValue!=null && propValue.toString().equals(value)) {
                    result.add(obj);
                }
            }
        }
        return result;
    }

    /**
     * Get the first element of a JsonArray.
     * Usage in Qute: {myArray.first}
     */
    static Object first(JsonArray array) {
        return array==null || array.isEmpty() ? null:array.getValue(0);
    }

    /**
     * Get the last element of a JsonArray.
     * Usage in Qute: {myArray.last}
     */
    static Object last(JsonArray array) {
        return array==null || array.isEmpty() ? null:array.getValue(array.size() - 1);
    }

    /**
     * Get the size of a JsonArray.
     * Usage in Qute: {myArray.size}
     */
    static int size(JsonArray array) {
        return array==null ? 0:array.size();
    }

    /**
     * Jekyll's "group_by" filter: group items by a property value.
     * Usage in Qute: {myArray.groupBy("type")}
     */
    static JsonArray groupBy(JsonArray items, String property) {
        if (items==null) {
            return new JsonArray();
        }
        Map<String, JsonArray> groups = new LinkedHashMap<>();
        for (int i = 0; i < items.size(); i++) {
            Object item = items.getValue(i);
            String key = "";
            if (item instanceof JsonObject obj) {
                Object val = obj.getValue(property);
                if (val!=null) {
                    key = val.toString();
                }
            }
            groups.computeIfAbsent(key, k -> new JsonArray()).add(item);
        }
        JsonArray result = new JsonArray();
        for (var entry : groups.entrySet()) {
            result.add(new JsonObject()
                .put("name", entry.getKey())
                .put("items", entry.getValue())
                .put("size", entry.getValue().size()));
        }
        return result;
    }

    private static final DateTimeFormatter RFC_822 = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    /**
     * RFC 822 date-time for LocalDateTime (assumes UTC).
     * Roq's built-in rfc822 only works on ZonedDateTime; this bridges the gap
     * for template globals like {now} which are LocalDateTime.
     * Usage in Qute: {now.rfc822}
     */
    static String rfc822(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneOffset.UTC).format(RFC_822);
    }

    /**
     * Jekyll's "xml_escape" / "escape" filter: escape HTML/XML special characters.
     * Qute auto-escapes in .html templates but not in .xml/.txt files.
     * Usage in Qute: {=myString.escapeHtml}
     */
    static String escapeHtml(String str) {
        if (str==null) {
            return "";
        }
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    /**
     * Jekyll/Liquid's "capitalize" filter: uppercase the first character.
     * Usage in Qute: {myString.capitalize}
     */
    static String capitalize(String str) {
        if (str==null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Jekyll's "truncate" filter: truncate a string to a given number of characters.
     * Usage in Qute: {myString.truncate(280)}
     */
    static String truncate(String str, int length) {
        if (str==null) {
            return "";
        }
        if (str.length() <= length) {
            return str;
        }
        return str.substring(0, length) + "...";
    }

    @SuppressWarnings("unchecked")
    static <T extends Comparable<T>> List<T> sort(List<T> list) {
        if (list==null || list.isEmpty()) {
            return List.of();
        }
        List<T> sorted = new ArrayList<>(list);
        Collections.sort(sorted);
        return sorted;
    }

    static List<?> sort(List<?> list, String property) {
        if (list==null || list.isEmpty()) {
            return List.of();
        }
        List<Object> sorted = new ArrayList<>(list);
        sorted.sort(propertyComparator(property));
        return sorted;
    }

    /**
     * A simple key/value pair whose properties are named exactly as the templates
     * expect: {@code data.first} for the key and {@code data.last} for the value.
     * Mirrors the shape of the entries produced by Roq's built-in JsonObject iteration.
     */
    public record JsonEntry(String first, Object last) {
    }

    /**
     * Jekyll's "sort" filter for JsonObject (map-of-maps, e.g. data/authors.yaml).
     * Returns a list of {@link JsonEntry} sorted by the named property on each value,
     * so templates can use {data.first} (key) and {data.last} (value).
     * Usage in Qute: {cdi:authors.sort('name')}
     */
    static List<JsonEntry> sort(JsonObject obj, String property) {
        if (obj==null || obj.isEmpty()) {
            return List.of();
        }
        List<JsonEntry> entries = new ArrayList<>();
        for (String key : obj.fieldNames()) {
            entries.add(new JsonEntry(key, obj.getValue(key)));
        }
        entries.sort((a, b) -> {
            String va = extractProperty(a.last(), property);
            String vb = extractProperty(b.last(), property);
            if (va==null) return vb==null ? 0:1;
            if (vb==null) return -1;
            return va.compareToIgnoreCase(vb);
        });
        return entries;
    }

    /**
     * Jekyll's "sort" filter for JsonArray.
     */
    static JsonArray sort(JsonArray array, String property) {
        if (array==null || array.isEmpty()) {
            return new JsonArray();
        }
        List<Object> sorted = new ArrayList<>(array.getList());
        sorted.sort(propertyComparator(property));
        return new JsonArray(sorted);
    }

    /**
     * Jekyll's "reverse" filter for JsonArray.
     */
    static JsonArray reverse(JsonArray array) {
        if (array==null || array.isEmpty()) {
            return new JsonArray();
        }
        List<Object> reversed = new ArrayList<>(array.getList());
        Collections.reverse(reversed);
        return new JsonArray(reversed);
    }

    /**
     * Filter items where the given boolean property is falsy.
     * Bridges Liquid's unless pattern.
     * Usage in Qute: {list:whereNot(myList, 'upcoming')}
     */
    @TemplateExtension(namespace = "list")
    static List<Object> whereNot(Iterable<?> items, String property) {
        List<Object> result = new ArrayList<>();
        if (items==null) {
            return result;
        }
        for (Object item : items) {
            try {
                Object value = getProperty(item, property);
                if (value==null || Boolean.FALSE.equals(value) || "false".equals(value.toString())) {
                    result.add(item);
                }
            } catch (Exception e) {
                result.add(item);
            }
        }
        return result;
    }

    @TemplateExtension(namespace = "list")
    static Object whereExp(Iterable<?> items, String loopVar, Object conditionsObj) {
        if (items==null) {
            return new ArrayList<>();
        }

        List<String> conditions;
        if (conditionsObj instanceof String s) {
            conditions = List.of(s);
        } else if (conditionsObj instanceof Iterable<?> iter) {
            conditions = new ArrayList<>();
            for (Object o : iter) {
                conditions.add(o.toString());
            }
        } else {
            conditions = List.of(conditionsObj.toString());
        }

        String prefix = loopVar + ".";
        boolean isJsonArray = items instanceof JsonArray;
        List<Object> result = isJsonArray ? new JsonArray().getList():new ArrayList<>();

        for (Object item : items) {
            boolean matches = true;
            for (String condition : conditions) {
                if (!evaluateCondition(item, prefix, condition)) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                result.add(item);
            }
        }
        return isJsonArray ? new JsonArray(result):result;
    }

    private static boolean evaluateCondition(Object item, String prefix, String condition) {
        String[] operators = {">=", "<=", "!=", "==", ">", "<", "contains"};
        for (String op : operators) {
            int idx = condition.indexOf(" " + op + " ");
            if (idx >= 0) {
                String left = condition.substring(0, idx).trim();
                String right = condition.substring(idx + op.length() + 2).trim();
                String property = left.startsWith(prefix) ? left.substring(prefix.length()):left;
                if ((right.startsWith("'") && right.endsWith("'"))
                    || (right.startsWith("\"") && right.endsWith("\"")))
                    right = right.substring(1, right.length() - 1);
                Object propValue = getProperty(item, property);
                String propStr = propValue!=null ? propValue.toString():null;
                return compareValues(propStr, op, right);
            }
        }
        return false;
    }

    private static boolean compareValues(String left, String op, String right) {
        if (left==null) {
            return false;
        }
        int cmp = left.compareTo(right);
        return switch (op) {
            case "==" -> cmp==0;
            case "!=" -> cmp!=0;
            case ">" -> cmp > 0;
            case ">=" -> cmp >= 0;
            case "<" -> cmp < 0;
            case "<=" -> cmp <= 0;
            case "contains" -> left.contains(right);
            default -> false;
        };
    }

    private static Object getProperty(Object obj, String property) {
        if (obj instanceof JsonObject json)
            return json.getValue(property);
        if (obj instanceof Map<?, ?> map)
            return map.get(property);
        // Try getter method (getX, isX, or plain x)
        Class<?> clazz = obj.getClass();
        for (String prefix : new String[]{"get", "is", ""}) {
            String methodName = prefix.isEmpty() ? property
                :prefix + Character.toUpperCase(property.charAt(0)) + property.substring(1);
            try {
                Method m = clazz.getMethod(methodName);
                return m.invoke(obj);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static String extractProperty(Object obj, String property) {
        Object val = getProperty(obj, property);
        if (val!=null) {
            return val.toString();
        }
        if (obj!=null && !(obj instanceof JsonObject) && !(obj instanceof Map)) {
            return obj.toString();
        }
        return null;
    }

    private static Comparator<Object> propertyComparator(String property) {
        return (a, b) -> {
            String va = extractProperty(a, property);
            String vb = extractProperty(b, property);
            if (va==null) {
                return vb==null ? 0:1;
            }
            if (vb==null) {
                return -1;
            }
            return va.compareToIgnoreCase(vb);
        };
    }

    static <T> List<T> distinct(List<T> list) {
        if (list==null || list.isEmpty()) {
            return List.of();
        }
        return list.stream().distinct().toList();
    }

    static JsonArray distinct(JsonArray array) {
        if (array==null || array.isEmpty()) {
            return new JsonArray();
        }
        List<Object> seen = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            Object val = array.getValue(i);
            if (!seen.contains(val)) {
                seen.add(val);
            }
        }
        return new JsonArray(seen);
    }

    /**
     * Jekyll's "push" filter: append an element to a list and return the new list.
     * Usage in Qute: {myList.push(item)}
     */
    static List<Object> push(List<?> list, Object item) {
        List<Object> result = new ArrayList<>(list);
        result.add(item);
        return result;
    }

    /**
     * Merge all items of a given type from all sources in a data index JsonObject.
     * Replaces the broken Jekyll push-accumulation pattern that doesn't work in Qute
     * ({#let} is block-scoped so push results are discarded in loops).
     * Usage in Qute: {index.mergeTypes('tutorial')}
     */
    @SuppressWarnings("unchecked")
    static JsonArray mergeTypes(JsonObject index, String type) {
        if (index==null || type==null || type.isEmpty()) {
            return null;
        }
        JsonArray result = new JsonArray();
        for (String key : index.fieldNames()) {
            Map<String, Object> sourceMap = toMap(index.getValue(key));
            if (sourceMap==null) {
                continue;
            }
            Map<String, Object> typesMap = toMap(sourceMap.get("types"));
            if (typesMap==null) {
                continue;
            }
            Object items = typesMap.get(type);
            if (items instanceof JsonArray arr) {
                for (Object item : arr) {
                    result.add(toJsonObject(item));
                }
            } else if (items instanceof List<?> list) {
                for (Object item : list) {
                    result.add(toJsonObject(item));
                }
            }
        }
        if (result.isEmpty()) {
            return null;
        }
        List<Object> sorted = new ArrayList<>(result.getList());
        sorted.sort(Comparator.comparing(
            o -> o instanceof JsonObject jo ? jo.getString("title", ""):"",
            String.CASE_INSENSITIVE_ORDER));
        return new JsonArray(sorted);
    }

    /**
     * True if the given data index contains a guide with the given URL, in any source or type.
     * Usage in Qute: {index.containsGuide('/guides/getting-started')}
     */
    static boolean containsGuide(JsonObject index, String url) {
        if (index==null || url==null || url.isEmpty()) {
            return false;
        }
        for (String key : index.fieldNames()) {
            Map<String, Object> sourceMap = toMap(index.getValue(key));
            if (sourceMap==null) {
                continue;
            }
            Map<String, Object> typesMap = toMap(sourceMap.get("types"));
            if (typesMap==null) {
                continue;
            }
            for (Object items : typesMap.values()) {
                for (Object item : listAsIterableOrEmpty(items)) {
                    JsonObject guide = toJsonObject(item);
                    if (guide!=null && url.equals(guide.getString("url"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * The subset of {@code versions} in which the guide at {@code guideUrl} exists, always
     * including {@code currentVersion}.
     * <p>
     * Usage in Qute: {@code {cdi:versioned.presentVersions(cdi:versions.documentation, guide_url, docversion)}}.
     */
    static List<String> presentVersions(Object versioned, Object versions, String guideUrl, String currentVersion) {
        List<String> present = new ArrayList<>();
        Map<String, Object> versionedMap = toMap(versioned);
        for (Object versionObj : listAsIterableOrEmpty(versions)) {
            if (versionObj==null) {
                continue;
            }
            String version = String.valueOf(versionObj);
            boolean found = version.equals(currentVersion);
            if (!found && versionedMap!=null) {
                Map<String, Object> versionMap = toMap(versionedMap.get(version.replace('.', '-')));
                if (versionMap!=null) {
                    found = containsGuide(toJsonObject(versionMap.get("index")), guideUrl);
                }
            }
            if (found) {
                present.add(version);
            }
        }
        return present;
    }

    private static Iterable<?> listAsIterableOrEmpty(Object items) {
        if (items instanceof JsonArray array) {
            return array;
        }
        if (items instanceof List<?> list) {
            return list;
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toMap(Object obj) {
        if (obj instanceof JsonObject jo)
            return jo.getMap();
        if (obj instanceof Map)
            return (Map<String, Object>) obj;
        return null;
    }

    @SuppressWarnings("unchecked")
    private static JsonObject toJsonObject(Object obj) {
        if (obj instanceof JsonObject jo)
            return jo;
        if (obj instanceof Map)
            return new JsonObject((Map<String, Object>) obj);
        return new JsonObject().put("value", obj);
    }

    /**
     * Jekyll's "markdownify" filter: convert Markdown text to HTML.
     * Returns a RawString to bypass Qute's auto-escaping.
     * Usage in Qute: {=myString.markdownify}
     */
    static RawString markdownify(String str) {
        if (str==null || str.isEmpty()) {
            return new RawString("");
        }
        return new RawString(str);
    }

    /**
     * Output a string without HTML escaping.
     * Qute auto-escapes HTML in .html templates; this bypasses that for trusted content.
     * Usage in Qute: {=myString.raw}
     */
    static RawString raw(String str) {
        if (str==null) {
            return new RawString("");
        }
        return new RawString(str);
    }

    // Liquid's {% assign %} is template-scoped; Qute's {#let} is block-scoped.
    // MutableMap bridges this: the converter emits mut:map() calls for variables
    // that would escape their {#let} scope.
    @TemplateExtension(namespace = "mut")
    static MutableMap map() {
        return new MutableMap();
    }

    public static class MutableMap {
        private final Map<String, Object> data = new HashMap<>();

        /**
         * Store a value under the given key (mirrors Liquid's assign).
         * Returns an empty RawString so {=_m.assign(...)} produces no visible output.
         */
        public RawString assign(String key, Object value) {
            data.put(key, value);
            return new RawString("");
        }

        public Object read(String key) {
            return data.get(key);
        }
    }

    /**
     * Split a string by delimiter, returning an iterable list.
     * Uses namespace form so it can handle null base objects (instance extensions can't
     * dispatch on null). Also returns List instead of String[] for Qute iteration.
     * Usage in Qute: {str:split(myString, ",")}
     */
    @TemplateExtension(namespace = "str")
    static List<String> split(String str, String delimiter) {
        if (str==null || str.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(str.split(Pattern.quote(delimiter)));
    }

    /**
     * Like split but returns RawStrings to prevent Qute auto-escaping of HTML content.
     * Usage in Qute: {str:splitRaw(myString, ",")}
     */
    @TemplateExtension(namespace = "str")
    static List<RawString> splitRaw(String str, String delimiter) {
        if (str==null || str.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(str.split(Pattern.quote(delimiter)))
            .map(RawString::new)
            .toList();
    }

    /**
     * Split, trim each element, and filter out empty strings.
     * Replaces the Liquid pattern: assign clean = "" | split: "" / for x in raw / push trimmed / endfor
     * That pattern doesn't work in Qute because {#let} is block-scoped (push results are discarded).
     * Usage in Qute: {str:splitTrimmed(myString, ",")}
     */
    @TemplateExtension(namespace = "str")
    static List<String> splitTrimmed(String str, String delimiter) {
        if (str==null || str.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String s : str.split(Pattern.quote(delimiter))) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * Resolve a guide URL for a given documentation version.
     * External links (http...) are returned as is.
     * Usage in Qute: {=guide.url.versionedGuideUrl(docversion)}
     */
    static String versionedGuideUrl(String url, String docversion) {
        if (url==null) {
            return "";
        }
        if (url.startsWith("http")) {
            return url;
        }
        String normalized = url.replaceAll("/+$", "") + "/";
        if (docversion==null || "latest".equals(docversion)) {
            return normalized;
        }
        return "/version/" + docversion + normalized;
    }

    /**
     * Build the categories object ({id: {title, description}}) consumed by the
     * <qs-categories-toc>/<qs-target> web components. Handles escaping.
     * Usage in Qute: {=cats.categoriesMetaJson.escapeHtml}
     */
    static String categoriesMetaJson(Object cats) {
        JsonObject meta = new JsonObject();
        for (Object item : listAsIterableOrEmpty(cats)) {
            JsonObject cat = toJsonObject(item);
            if (cat==null) {
                continue;
            }
            String id = cat.getString("id");
            if (id==null) {
                continue;
            }
            JsonObject entry = new JsonObject().put("title", cat.getString("title"));
            String description = cat.getString("description");
            if (description!=null) {
                entry.put("description", description);
            }
            meta.put(id, entry);
        }
        return meta.encode();
    }

    /**
     * Build the categories TOC JSON array ([{id, title, subcategories:[{id, title}]}])
     * consumed by the <qs-categories-toc> web component.
     * Usage in Qute: {=cats.categoriesTocJson.escapeHtml}
     */
    static String categoriesTocJson(Object cats) {
        JsonArray toc = new JsonArray();
        for (Object item : listAsIterableOrEmpty(cats)) {
            JsonObject cat = toJsonObject(item);
            if (cat==null) {
                continue;
            }
            JsonObject node = new JsonObject()
                .put("id", cat.getString("id", ""))
                .put("title", cat.getString("title", ""));
            JsonArray subArr = new JsonArray();
            for (Object subItem : listAsIterableOrEmpty(cat.getValue("subcategories"))) {
                JsonObject sub = toJsonObject(subItem);
                if (sub!=null) {
                    subArr.add(new JsonObject()
                        .put("id", sub.getString("id", ""))
                        .put("title", sub.getString("title", "")));
                }
            }
            if (!subArr.isEmpty()) {
                node.put("subcategories", subArr);
            }
            toc.add(node);
        }
        return toc.encode();
    }

    // Jekyll append filter: string concatenation
    // {{ language.url | append: path }} → {=language.url.append(path)}
    static String append(Object base, Object suffix) {
        String baseStr = base!=null ? base.toString():"";
        String suffixStr = suffix!=null ? suffix.toString():"";
        return baseStr + suffixStr;
    }

    // Jekyll prepend filter: string concatenation (reversed)
    // {{ path | prepend: language.url }} → {=path.prepend(language.url)}
    static String prepend(Object suffix, Object prefix) {
        String prefixStr = prefix!=null ? prefix.toString():"";
        String suffixStr = suffix!=null ? suffix.toString():"";
        return prefixStr + suffixStr;
    }
}
