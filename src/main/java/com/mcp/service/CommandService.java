package com.mcp.service;

public interface CommandService {
    String runCommand(String command);

    String listProcesses();

    String terminateProcess(String processNameOrId);
}
