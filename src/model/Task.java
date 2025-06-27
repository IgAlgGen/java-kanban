package model;

import utils.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private int id;
    protected String name;
    protected String description;
    protected Status status;
    protected LocalDateTime startTime; //Дата и время начала выполнения задачи
    protected Duration duration; //Продолжительность задачи в минутах

    public Task(int id, String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        this(0, name, description, status, startTime, duration);
    }

    //region Геттеры и сеттеры
    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getType() {
        return getClass().getSimpleName();
    }

    public int getId() {
        return id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setName(String string) {
        this.name = string;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    //endregion
    //region equals и hashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(name, task.name) && Objects.equals(description, task.description) && status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, status);
    }
    //endregion

    /**
     * Возвращает время окончания задачи, которое рассчитывается как
     * время начала плюс продолжительность задачи.
     *
     * @return Время окончания задачи.
     */
    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", startTime=" + startTime +
                ", duration=" + duration +
                '}';
    }
}
