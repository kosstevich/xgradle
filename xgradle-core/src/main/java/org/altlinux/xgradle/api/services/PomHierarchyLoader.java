package org.altlinux.xgradle.api.services;

import org.altlinux.xgradle.impl.model.MavenCoordinate;

import java.util.List;

public interface PomHierarchyLoader {

    List<MavenCoordinate> loadHierarchy(MavenCoordinate root);
}
