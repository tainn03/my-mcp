package com.mcp.service.impl;

import com.mcp.service.CommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Scanner;

@Service
@RequiredArgsConstructor
public class CommandServiceImpl implements CommandService {
    /**
     * Run a system command and return the output.
     *
     * @param command the system command to run
     * @return the output of the command
     */
    @Override
    public String runCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStream is = process.getInputStream();
            Scanner s = new Scanner(is).useDelimiter("\\A");
            String output = s.hasNext() ? s.next() : "";
            int exitCode = process.waitFor();
            return "EXIT CODE: " + exitCode + "\nOUTPUT:\n" + output;
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * List all running processes on the system.
     *
     * @return a list of running processes
     */
    @Override
    public String listProcesses() {
        StringBuilder builder = new StringBuilder();
        try {
            ProcessHandle.allProcesses().forEach(ph -> {
                builder.append("PID: ").append(ph.pid())
                        .append(", Command: ").append(ph.info().command().orElse("N/A"))
                        .append("\n");
            });
            return builder.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Find and terminate a process by its name or ID.
     *
     * @param processNameOrId the name or ID of the process to terminate
     * @return the result of the termination attempt
     */
    @Override
    public String terminateProcess(String processNameOrId) {
        try {
            try {
                long pid = Long.parseLong(processNameOrId);
                return terminateProcessByPID(pid);
            } catch (NumberFormatException nfe) {
                return terminateProcessByName(processNameOrId);
            }
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Terminate a process by its PID.
     *
     * @param pid the PID of the process to terminate
     * @return the result of the termination attempt
     */
    private String terminateProcessByPID(long pid) {
        ProcessHandle processHandle = ProcessHandle.of(pid).orElse(null);
        if (processHandle != null && processHandle.destroy()) {
            return "TERMINATED PROCESS WITH PID: " + pid;
        } else {
            return "COULD NOT FIND OR TERMINATE PROCESS WITH PID: " + pid;
        }
    }

    /**
     * Terminate a process by its name.
     *
     * @param processName the name of the process to terminate
     * @return the result of the termination attempt
     */
    private String terminateProcessByName(String processName) {
        for (ProcessHandle processHandle : ProcessHandle.allProcesses().toList()) {
            String cmd = processHandle.info().command().orElse("");
            if (cmd.contains(processName)) {
                if (processHandle.destroy()) {
                    return "TERMINATED PROCESS: " + cmd + " (PID: " + processHandle.pid() + ")";
                }
            }
        }
        return "NO PROCESS FOUND WITH NAME OR ID: " + processName;
    }
}
