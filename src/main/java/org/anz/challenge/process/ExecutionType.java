package org.anz.challenge.process;

public enum ExecutionType {
    Sequential("1"),
    Parallel("2"),
    SingleThread("3"),
    MultiThread("4");

    private final String executionFlag;

    ExecutionType(String executionFlag) {
        this.executionFlag = executionFlag;
    }

    public static ExecutionType fromFlag(String executionFlag) {
        for (ExecutionType executionType : values()) {
            if (executionFlag.equals(executionType.executionFlag)) {
                return executionType;
            }
        }
        return Sequential;
    }
}
