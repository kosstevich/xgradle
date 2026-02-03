package org.altlinux.xgradle.impl.parsers;

import org.altlinux.xgradle.impl.model.MavenCoordinate;
import org.altlinux.xgradle.impl.model.MavenCoordinateBuilder;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses direct dependencies from a Maven POM hierarchy
 * and applies dependencyManagement rules.
 *
 * @author Ivan Khanas <xeno@altlinux.org>
 */
final class PomDependenciesParser {

    Map<String, MavenCoordinate> parse(
            List<Model> pomHierarchy,
            Map<String, String> properties,
            Map<String, MavenCoordinate> managedByGroupAndArtifact,
            PomPropertiesCollector propertiesCollector
    ) {
        Map<String, MavenCoordinate> resolvedByGroupAndArtifact =
                new LinkedHashMap<>();

        if (pomHierarchy == null) {
            return resolvedByGroupAndArtifact;
        }

        for (Model model : pomHierarchy) {
            if (model == null || model.getDependencies() == null) {
                continue;
            }

            for (Dependency dependency : model.getDependencies()) {
                MavenCoordinate coordinate =
                        PomDependencyUtils.convertDependency(dependency);

                coordinate =
                        PomDependencyUtils.resolveProperties(
                                coordinate,
                                properties,
                                propertiesCollector
                        );

                coordinate =
                        applyDependencyManagement(
                                coordinate,
                                managedByGroupAndArtifact
                        );

                if (!coordinate.isValid()) {
                    continue;
                }

                String groupAndArtifact =
                        coordinate.getGroupId()
                                + ":" + coordinate.getArtifactId();

                resolvedByGroupAndArtifact.put(
                        groupAndArtifact,
                        coordinate
                );
            }
        }
        return resolvedByGroupAndArtifact;
    }

    private MavenCoordinate applyDependencyManagement(
            MavenCoordinate coordinate,
            Map<String, MavenCoordinate> managedByGroupAndArtifact
    ) {
        MavenCoordinate managed =
                managedByGroupAndArtifact.get(
                        coordinate.getGroupId()
                                + ":" + coordinate.getArtifactId()
                );

        if (managed == null) {
            return coordinate;
        }

        MavenCoordinateBuilder builder = coordinate.toBuilder();

        if (coordinate.getVersion() == null
                || coordinate.getVersion().isEmpty()) {
            builder.version(managed.getVersion());
        }

        if (coordinate.getScope() == null) {
            builder.scope(managed.getScope());
        }

        if (coordinate.getPackaging() == null
                || coordinate.getPackaging().isEmpty()) {
            builder.packaging(managed.getPackaging());
        }

        return builder.build();
    }
}
