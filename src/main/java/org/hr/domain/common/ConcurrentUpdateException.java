package org.hr.domain.common;

/** RQ-12: modificare concurenta respinsa prin versionare optimista. */
public class ConcurrentUpdateException extends RuntimeException {
    public ConcurrentUpdateException(String entityName, Long id, Long attemptedVersion, Long storedVersion) {
        super("Concurrent modification of " + entityName + "#" + id
                + ": attempted version " + attemptedVersion + " but stored version is " + storedVersion);
    }
}
