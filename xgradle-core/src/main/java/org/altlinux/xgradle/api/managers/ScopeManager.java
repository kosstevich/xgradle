package org.altlinux.xgradle.api.managers;

public interface ScopeManager {

    void updateScope(String dependencyKey, String newScope);

    String getScope(String dependencyKey);
}
