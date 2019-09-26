package app.weasel.net.api.github;

public final class Tag {
    private String name;
    private String zipball_url;
    private String tarball_url;
    private Commit commit;
    private String node_id;

    public String getName() {
        return name;
    }

    public Commit getCommit() {
        return commit;
    }

    public String getNodeId() {
        return node_id;
    }

    public String getTarballUrl() {
        return tarball_url;
    }

    public String getZipballUrl() {
        return zipball_url;
    }
}
