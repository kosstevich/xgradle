/*
 * Copyright 2025 BaseALT Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.altlinux.xgradle.impl.parsers;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.caches.PomDataCache;
import org.altlinux.xgradle.api.maven.PomHierarchyLoader;
import org.altlinux.xgradle.api.parsers.PomParser;
import org.altlinux.xgradle.impl.model.MavenCoordinate;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import java.nio.file.Path;
import java.util.*;

/**
 * Implementation of {@link PomParser} that parses Maven POM files
 * with caching for improved performance.
 *
 * <p>This parser provides:
 * <ul>
 *   <li>Basic POM information parsing</li>
 *   <li>Dependency management section parsing</li>
 *   <li>Dependencies parsing</li>
 *   <li>Properties parsing</li>
 *   <li>Multi-level caching for parsed results</li>
 * </ul>
 *
 * <p>Cache types:
 * <ul>
 *   <li>POM_CACHE: Main POM coordinates</li>
 *   <li>DEP_MGMT_CACHE: Dependency management entries</li>
 *   <li>DEPENDENCIES_CACHE: Direct dependencies</li>
 *   <li>PROPERTIES_CACHE: Properties section</li>
 * </ul>
 *
 * <p>Parsing handles:
 * <ul>
 *   <li>Basic project coordinates</li>
 *   <li>Inheritance from parent POM</li>
 *   <li>Default values for packaging and scope</li>
 *   <li>Namespace-agnostic element lookup</li>
 * </ul>
 *
 * @author Ivan Khanas
 */
@Singleton
class DefaultPomParser implements PomParser {

    private final PomDataCache pomDataCache;
    private final PomHierarchyLoader pomHierarchyLoader;

    @Inject
    DefaultPomParser(PomDataCache pomDataCache, PomHierarchyLoader pomHierarchyLoader) {
        this.pomDataCache = pomDataCache;
        this.pomHierarchyLoader = pomHierarchyLoader;
    }

    /**
     * Parses the main coordinates from a POM file.
     *
     * <p>Handles:
     * <ul>
     *   <li>Direct child elements (artifactId, groupId, version)</li>
     *   <li>Parent inheritance when groupId/version are missing</li>
     *   <li>Default packaging type (jar)</li>
     *   <li>Validation of required fields</li>
     * </ul>
     *
     * @param pomPath path to the POM file
     *
     * @return MavenCoordinate object with parsed data, or null if parsing fails
     */
    @Override
    public MavenCoordinate parsePom(Path pomPath) {
        String cacheKey = pomPath.toString();

        MavenCoordinate cached = pomDataCache.getPom(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<Model> hierarchy = pomHierarchyLoader.loadHierarchy(pomPath);
        if (hierarchy == null || hierarchy.isEmpty()) {
            return null;
        }

        Model effective = hierarchy.get(hierarchy.size() - 1);
        MavenCoordinate coord = createCoordinate(effective, pomPath);
        if (coord != null) {
            pomDataCache.putPom(cacheKey, coord);
        }
        return coord;
    }

    /**
     * Creates a {@link MavenCoordinate} object from a Maven {@link Model}.
     *
     * @param model   parsed Maven model
     * @param pomPath path to the POM file
     *
     * @return fully populated MavenCoordinate
     */
    private MavenCoordinate createCoordinate(Model model, Path pomPath) {
        if (model == null) {
            return null;
        }

        MavenCoordinate coord = new MavenCoordinate();
        coord.setPomPath(pomPath);
        coord.setArtifactId(model.getArtifactId());
        coord.setVersion(model.getVersion());
        coord.setGroupId(model.getGroupId());
        coord.setPackaging(model.getPackaging());

        if (isEmpty(coord.getGroupId()) && model.getParent() != null) {
            coord.setGroupId(model.getParent().getGroupId());
        }
        if (isEmpty(coord.getVersion()) && model.getParent() != null) {
            coord.setVersion(model.getParent().getVersion());
        }
        if (isEmpty(coord.getPackaging())) {
            coord.setPackaging("jar");
        }
        return coord;
    }

    /**
     * Parses properties section of a POM file.
     *
     * @param pomPath path to the POM file
     *
     * @return map of property names to values {@code @NotNull}
     */
    @Override
    public Map<String, String> parseProperties(Path pomPath) {
        String cacheKey = pomPath.toString();

        Map<String, String> cached = pomDataCache.getProperties(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<Model> hierarchy = pomHierarchyLoader.loadHierarchy(pomPath);
        Map<String, String> properties = collectProperties(hierarchy);

        Map<String, String> unmodifiable =
                Collections.unmodifiableMap(new LinkedHashMap<>(properties));

        pomDataCache.putProperties(cacheKey, unmodifiable);
        return unmodifiable;
    }

    /**
     * Collects and merges all properties from a POM hierarchy.
     *
     * @param hierarchy list of Maven models (ordered parent -> child)
     *
     * @return merged properties map
     */
    private Map<String, String> collectProperties(List<Model> hierarchy) {
        Map<String, String> props = new LinkedHashMap<>();
        props.putIfAbsent("project.build.sourceEncoding", "UTF-8");
        props.putIfAbsent("project.reporting.outputEncoding", "UTF-8");

        if (hierarchy == null) {
            return props;
        }

        for (Model model : hierarchy) {
            if (model == null) {
                continue;
            }

            putIfNotEmpty(props, "project.groupId", model.getGroupId());
            putIfNotEmpty(props, "groupId", model.getGroupId());
            putIfNotEmpty(props, "project.artifactId", model.getArtifactId());
            putIfNotEmpty(props, "artifactId", model.getArtifactId());
            putIfNotEmpty(props, "project.version", model.getVersion());
            putIfNotEmpty(props, "version", model.getVersion());
            putIfNotEmpty(props, "project.packaging", model.getPackaging());
            putIfNotEmpty(props, "packaging", model.getPackaging());

            if (model.getProperties() != null) {
                model.getProperties().forEach((k, v) -> {
                    if (k != null && v != null) {
                        props.put(k.toString(), v.toString());
                    }
                });
            }
        }
        return props;
    }

    private void putIfNotEmpty(Map<String, String> map, String key, String value) {
        if (!isEmpty(value)) {
            map.putIfAbsent(key, value);
        }
    }

    /**
     * Parses the dependency management section of a POM file.
     *
     * @param pomPath path to the POM file
     *
     * @return list of managed dependencies {@code @NotNull}
     */
    @Override
    public ArrayList<MavenCoordinate> parseDependencyManagement(Path pomPath) {
        String cacheKey = pomPath.toString();

        ImmutableList<MavenCoordinate> cached = pomDataCache.getDependencyManagement(cacheKey);
        if (cached != null) {
            return new ArrayList<>(cached);
        }

        List<Model> hierarchy = pomHierarchyLoader.loadHierarchy(pomPath);
        if (hierarchy == null || hierarchy.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, String> properties = collectProperties(hierarchy);

        ImmutableList.Builder<MavenCoordinate> builder = ImmutableList.builder();
        collectDependencyManagement(hierarchy, properties, builder);
        ImmutableList<MavenCoordinate> immutable = builder.build();

        pomDataCache.putDependencyManagement(cacheKey, immutable);
        return new ArrayList<>(immutable);
    }

    /**
     * Appends managed dependencies to the provided builder.
     *
     * @param hierarchy  list of Maven models (ordered parent -> child)
     * @param properties resolved property map
     * @param out        builder to append resulting coordinates to
     */
    private void collectDependencyManagement(
            List<Model> hierarchy, Map<String, String> properties, ImmutableList.Builder<MavenCoordinate> out
    ) {
        if (hierarchy == null) {
            return;
        }

        Map<String, MavenCoordinate> seen = new LinkedHashMap<>();

        for (Model model : hierarchy) {
            if (model == null || model.getDependencyManagement() == null) {
                continue;
            }

            if (model.getDependencyManagement().getDependencies() == null) {
                continue;
            }

            for (Dependency dep : model.getDependencyManagement().getDependencies()) {
                MavenCoordinate coord = convertDependency(dep);
                resolveProperties(coord, properties);
                if (!coord.isValid()) {
                    continue;
                }

                String key = coord.getGroupId() + ":" + coord.getArtifactId();
                seen.put(key, coord);
            }
        }

        for (MavenCoordinate coord : seen.values()) {
            out.add(coord);
        }
    }

    /**
     * Parses direct dependencies from a POM file.
     *
     * @param pomPath path to the POM file
     *
     * @return list of direct dependencies {@code @NotNull}
     */
    @Override
    public ArrayList<MavenCoordinate> parseDependencies(Path pomPath) {
        String cacheKey = pomPath.toString();

        ImmutableList<MavenCoordinate> cached = pomDataCache.getDependencies(cacheKey);
        if (cached != null) {
            return new ArrayList<>(cached);
        }

        List<Model> hierarchy = pomHierarchyLoader.loadHierarchy(pomPath);
        if (hierarchy == null || hierarchy.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, String> properties = collectProperties(hierarchy);

        ImmutableList.Builder<MavenCoordinate> mgBuilder = ImmutableList.builder();
        collectDependencyManagement(hierarchy, properties, mgBuilder);

        Map<String, MavenCoordinate> depMgmtMap = new HashMap<>();
        for (MavenCoordinate m : mgBuilder.build()) {
            if (m.getGroupId() != null && m.getArtifactId() != null) {
                depMgmtMap.put(m.getGroupId() + ":" + m.getArtifactId(), m);
            }
        }

        ArrayList<MavenCoordinate> resolved =
                extractAllDependencies(hierarchy, properties, depMgmtMap);

        pomDataCache.putDependencies(cacheKey, ImmutableList.copyOf(resolved));
        return resolved;
    }

    private ArrayList<MavenCoordinate> extractAllDependencies(
            List<Model> hierarchy,
            Map<String, String> properties,
            Map<String, MavenCoordinate> depMgmtMap
    ) {
        Map<String, MavenCoordinate> allDeps = new LinkedHashMap<>();
        if (hierarchy == null) {
            return new ArrayList<>();
        }

        for (Model model : hierarchy) {
            if (model == null || model.getDependencies() == null) {
                continue;
            }

            for (Dependency dep : model.getDependencies()) {
                MavenCoordinate coord = convertDependency(dep);
                resolveProperties(coord, properties);
                if (coord.getGroupId() != null && coord.getArtifactId() != null) {
                    allDeps.put(coord.getGroupId() + ":" + coord.getArtifactId(), coord);
                }
            }
        }

        ArrayList<MavenCoordinate> result = new ArrayList<>();
        for (MavenCoordinate coord : allDeps.values()) {
            applyDependencyManagement(coord, depMgmtMap);
            if (coord.isValid()) {
                result.add(coord);
            }
        }
        return result;
    }

    private void applyDependencyManagement(
            MavenCoordinate coord,
            Map<String, MavenCoordinate> depMgmtMap
    ) {
        if (coord == null || coord.getGroupId() == null || coord.getArtifactId() == null) {
            return;
        }

        MavenCoordinate managed =
                depMgmtMap.get(coord.getGroupId() + ":" + coord.getArtifactId());

        if (managed == null) {
            return;
        }

        if (isEmpty(coord.getVersion())) {
            coord.setVersion(managed.getVersion());
        }
        if (isEmpty(coord.getScopeValue())) {
            coord.setScope(managed.getScope());
        }
        if (isEmpty(coord.getPackaging())) {
            coord.setPackaging(managed.getPackaging());
        }
    }

    private void resolveProperties(
            MavenCoordinate coord,
            Map<String, String> properties
    ) {
        if (coord == null) {
            return;
        }
        coord.setGroupId(resolveProperty(coord.getGroupId(), properties));
        coord.setArtifactId(resolveProperty(coord.getArtifactId(), properties));
        coord.setVersion(resolveProperty(coord.getVersion(), properties));
        coord.setPackaging(resolveProperty(coord.getPackaging(), properties));
        coord.setScope(resolveProperty(coord.getScopeValue(), properties));
    }

    private String resolveProperty(String value, Map<String, String> properties) {
        if (value == null || properties == null) {
            return value;
        }

        String current = value;
        for (int i = 0; i < 20; i++) {
            StringBuilder b = new StringBuilder();
            int pos = 0;
            boolean changed = false;

            while (pos < current.length()) {
                int start = current.indexOf("${", pos);
                if (start == -1) {
                    b.append(current.substring(pos));
                    break;
                }

                int end = current.indexOf('}', start + 2);
                if (end == -1) {
                    b.append(current.substring(pos));
                    break;
                }

                b.append(current, pos, start);
                String key = current.substring(start + 2, end);
                String replacement = properties.get(key);

                if (replacement != null) {
                    b.append(replacement);
                    changed = true;
                } else {
                    b.append("${").append(key).append("}");
                }

                pos = end + 1;
            }

            if (!changed) {
                break;
            }
            current = b.toString();
        }
        return current;
    }

    private MavenCoordinate convertDependency(Dependency dep) {
        MavenCoordinate coord = new MavenCoordinate();
        if (dep == null) {
            return coord;
        }

        coord.setGroupId(dep.getGroupId());
        coord.setArtifactId(dep.getArtifactId());
        coord.setVersion(dep.getVersion());
        coord.setScope(dep.getScope() != null ? dep.getScope() : "compile");
        coord.setPackaging(dep.getType() != null ? dep.getType() : "jar");
        return coord;
    }

    /**
     * Null- and blank-safe string check.
     *
     * @param str input string
     * @return true if {@code str} is null or empty after trimming
     */
    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
