public enum Progress {
    NEW(0),
    IN_PROGRESS(1),
    DONE(2);

    private int value;

    Progress(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
