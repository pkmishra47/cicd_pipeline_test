package org.apache.nifi.processors.daxoperation.dm;

public class ProcessorStopRequest {
    public Revision revision;
    public String state;
    public Boolean disconnectedNodeAcknowledged;
}
