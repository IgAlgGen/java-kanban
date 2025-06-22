package model;

import utils.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtaskIDs;
    private Duration duration; // Сумма duration всех подзадач.
    private LocalDateTime startTime; // Минимум startTime среди подзадач.
    private LocalDateTime endTime; // Максимум getEndTime() среди подзадач.

    public Epic(int id, String name, String description, Status status, Duration duration, LocalDateTime startTime, LocalDateTime endTime) {
        super(id, name, description, status, startTime, duration);
        this.subtaskIDs = new ArrayList<>();
        this.duration = duration != null ? duration : Duration.ZERO;
    }

    public Epic(String name, String description, Status status, Duration duration, LocalDateTime startTime, LocalDateTime endTime) {
        this(0, name, description, status, duration, startTime, endTime);
    }

    public List<Integer> getSubtaskIDs() {
        return subtaskIDs;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIDs.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIDs.remove(Integer.valueOf(subtaskId));
    }

    public void clearSubtaskIds() {
        subtaskIDs.clear();
    }

    /**
     * Пересчитывает временные параметры эпика на основе переданных подзадач.
     * Этот метод обновляет duration, startTime и endTime эпика
     * на основании подзадач, входящих в данный эпик.
     *
     * @param subtasks Список подзадач, по которым пересчитываются временные параметры эпика.
     */
    public void recalculateEpicTimeDetails(List<Subtask> subtasks) {
        for (Subtask subtask : subtasks) {
            if (subtaskIDs.contains(subtask.getId())) {
                if (duration == null) {
                    duration = Duration.ZERO;
                }else {
                    duration = duration.plus(subtask.getDuration());
                }

                if (startTime == null || subtask.getStartTime().isBefore(startTime)) {
                    startTime = subtask.getStartTime();
                }

                LocalDateTime subtaskEndTime = subtask.getEndTime();
                if (endTime == null || subtaskEndTime.isAfter(endTime)) {
                    endTime = subtaskEndTime;
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIDs, epic.subtaskIDs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIDs);
    }
}
