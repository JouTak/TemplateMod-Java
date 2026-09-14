package ru.joutak.templatemod.client;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

public class RoutRecorder {
    private final int recordingIntervalTicks;
    public RoutRecorder(int recordingIntervalTicks) {
        this.recordingIntervalTicks = recordingIntervalTicks;
    }
    public enum RecordingState {
        STOPPED,
        RECORDING,
        PAUSED
    }
    public void start(Vec3 position) {
        points.clear();
        points.add(position);
        this.state = RecordingState.RECORDING;
        this.ticksSinceLastPoint = 0;
    }

    public void tick(Vec3 position) {
        if (this.state != RecordingState.RECORDING) {
            return;
        }
        this.ticksSinceLastPoint ++;
        if (this.ticksSinceLastPoint >= this.recordingIntervalTicks) {
            if (!points.getLast().equals(position)) {
                points.add(position);
            }
            this.ticksSinceLastPoint = 0;
        }
    }
    public void toggleRecording(Vec3 position) {
        if (this.state == RecordingState.STOPPED) {
            start(position);
        }
        else if (this.state == RecordingState.RECORDING || this.state == RecordingState.PAUSED) {
            this.state = RecordingState.STOPPED;
            this.ticksSinceLastPoint = 0;
        }
    }
    public void togglePause() {
        if (this.state == RecordingState.RECORDING) {
            this.state = RecordingState.PAUSED;
        } else if (this.state == RecordingState.PAUSED) {
            this.state = RecordingState.RECORDING;
        }
    }
    public void clear() {
        this.points.clear();
        this.state = RecordingState.STOPPED;
        this.ticksSinceLastPoint = 0;
    }
    public List<Vec3> getPoints() {
        return List.copyOf(points);
    }
    public RecordingState getState() {
        return state;
    }
    private RecordingState state = RecordingState.STOPPED;
    private int ticksSinceLastPoint = 0;
    private ArrayList<Vec3> points = new ArrayList<>();

}
