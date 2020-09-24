package com.sci.valerie.compiler;

import com.sci.valerie.parser.tree.*;

public final class WorkStatus {
    public final boolean finished;
    public final Node waitingOn;
    public final WaitType waitType;

    public WorkStatus(final boolean finished, final Node waitingOn, final WaitType waitType) {
        this.finished = finished;
        this.waitingOn = waitingOn;
        this.waitType = waitType;
    }
}