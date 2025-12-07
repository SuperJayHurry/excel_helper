package org.example.entity.enums;

public enum Department {
    COMPUTER_SCIENCE("计算机系"),
    MATHEMATICS("数学系"),
    PHYSICS("物理系"),
    CHEMISTRY("化学系"),
    MECHANICS("力学系");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

