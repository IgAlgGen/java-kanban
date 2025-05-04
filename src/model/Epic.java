package model;

import utils.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtaskIDs;

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
        this.subtaskIDs = new ArrayList<>();
    }
    public Epic(String name, String description, Status status) {
        this(0, name, description, status);
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
