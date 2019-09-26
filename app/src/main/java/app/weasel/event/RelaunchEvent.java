package app.weasel.event;

public final class RelaunchEvent implements Event {
    private final int fragment;

    public RelaunchEvent(int fragment) {
        this.fragment = fragment;
    }

    public int getFragment() {
        return fragment;
    }
}
