package app.weasel.event;

public final class SongDiscoveryEvent {

    public enum EventCode {
        song_discovered,
        discovery_finished
    }

    private final String songTitle;
    private final EventCode eventCode;

    public SongDiscoveryEvent() {
        this.eventCode = EventCode.discovery_finished;
        this.songTitle = null;
    }

    public SongDiscoveryEvent(String songTitle) {
        this.songTitle = songTitle;
        this.eventCode = EventCode.song_discovered;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public EventCode getEventCode() {
        return eventCode;
    }
}
