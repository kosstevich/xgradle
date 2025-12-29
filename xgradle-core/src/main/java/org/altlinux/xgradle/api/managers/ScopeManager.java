package org.altlinux.xgradle.api.managers;

import org.altlinux.xgradle.impl.enums.MavenScope;

public interface ScopeManager {

    /**
     * Updates the stored scope for a dependency if the new scope has higher priority.
     *
     * @param dependencyKey dependency identifier (for example "groupId:artifactId")
     * @param newScope new scope candidate
     */
    void updateScope(String dependencyKey, MavenScope newScope);

    MavenScope getScope(String dependencyKey);
}
