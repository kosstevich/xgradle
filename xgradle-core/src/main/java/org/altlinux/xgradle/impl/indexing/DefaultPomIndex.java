package org.altlinux.xgradle.impl.indexing;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.altlinux.xgradle.api.collectors.PomFilesCollector;
import org.altlinux.xgradle.api.indexing.PomIndex;
import org.altlinux.xgradle.api.parsers.PomParser;
import org.altlinux.xgradle.impl.model.MavenCoordinate;
import org.gradle.api.logging.Logger;

import java.nio.file.Path;
import java.util.*;

@Singleton
final class DefaultPomIndex implements PomIndex {

    private final PomFilesCollector pomFilesCollector;
    private final PomParser pomParser;
    private final Logger logger;

    private volatile Map<String, MavenCoordinate> byGa = new LinkedHashMap<>();
    private volatile Map<String, List<MavenCoordinate>> byGroup = new LinkedHashMap<>();

    @Inject
    DefaultPomIndex(PomFilesCollector pomFilesCollector, PomParser pomParser, Logger logger) {
        this.pomFilesCollector = pomFilesCollector;
        this.pomParser = pomParser;
        this.logger = logger;
    }

    @Override
    public synchronized void build(Path rootDirectory) {
        List<Path> files = pomFilesCollector.collect(rootDirectory);
        build(files);
    }

    @Override
    public synchronized void build(List<Path> pomFiles) {
        Map<String, MavenCoordinate> newByGa = new LinkedHashMap<>();
        Map<String, List<MavenCoordinate>> newByGroup = new LinkedHashMap<>();

        for (Path pomPath : pomFiles) {
            MavenCoordinate coord = pomParser.parsePom(pomPath);
            if (coord == null) {
                continue;
            }

            String ga = coord.getGroupId() + ":" + coord.getArtifactId();

            MavenCoordinate prev = newByGa.get(ga);
            if (prev == null || isNewer(coord.getVersion(), prev.getVersion())) {
                newByGa.put(ga, coord);
            }

            newByGroup.computeIfAbsent(coord.getGroupId(), k -> new ArrayList<>()).add(coord);
        }

        for (Map.Entry<String, List<MavenCoordinate>> e : newByGroup.entrySet()) {
            e.getValue().sort(Comparator.comparing(MavenCoordinate::getArtifactId)
                    .thenComparing(MavenCoordinate::getVersion, this::compareVersions));
        }

        byGa = newByGa;
        byGroup = newByGroup;

        logger.lifecycle("POM index built: {} artifacts, {} groups", byGa.size(), byGroup.size());
    }

    @Override
    public Optional<MavenCoordinate> find(String groupId, String artifactId) {
        return Optional.ofNullable(byGa.get(groupId + ":" + artifactId));
    }

    @Override
    public List<MavenCoordinate> findAllForGroup(String groupId) {
        return byGroup.getOrDefault(groupId, List.of());
    }

    @Override
    public Map<String, MavenCoordinate> snapshot() {
        return Collections.unmodifiableMap(byGa);
    }

    private boolean isNewer(String a, String b) {
        return compareVersions(a, b) > 0;
    }

    private int compareVersions(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        String[] pa = a.split("[.-]");
        String[] pb = b.split("[.-]");
        int n = Math.max(pa.length, pb.length);
        for (int i = 0; i < n; i++) {
            String sa = i < pa.length ? pa[i] : "0";
            String sb = i < pb.length ? pb[i] : "0";
            int cmp = comparePart(sa, sb);
            if (cmp != 0) return cmp;
        }
        return 0;
    }

    private int comparePart(String a, String b) {
        boolean na = a.chars().allMatch(Character::isDigit);
        boolean nb = b.chars().allMatch(Character::isDigit);

        if (na && nb) {
            int ia = Integer.parseInt(a);
            int ib = Integer.parseInt(b);
            return Integer.compare(ia, ib);
        }

        if (na) return 1;
        if (nb) return -1;

        return a.compareTo(b);
    }
}
