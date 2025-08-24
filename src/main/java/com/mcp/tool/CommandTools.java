package com.mcp.tool;

import com.mcp.service.CommandService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommandTools {
    CommandService commandService;

    /**
     * Run a system command and return the output.
     *
     * @param command the system command to run
     * @return the output of the command
     */
    @Tool(name = "c01_run_command", description = "Run a system command and return the output.")
    public String runCommand(@ToolParam String command) {
        if (command == null || command.isBlank()) {
            return "Command is empty.";
        }
        return commandService.runCommand(command);
    }

    /**
     * List all running processes on the system.
     *
     * @return a list of running processes
     */
    @Tool(name = "c02_list_processes", description = "List all running processes on the system.")
    public String listProcesses() {
        return commandService.listProcesses();
    }

    /**
     * Find and terminate a process by its name or ID.
     *
     * @param processNameOrId the name or ID of the process to terminate
     * @return the result of the termination attempt
     */
    @Tool(name = "c03_terminate_process", description = "Find and terminate a process by its name or ID.")
    public String terminateProcess(@ToolParam String processNameOrId) {
        if (processNameOrId == null || processNameOrId.isBlank()) {
            return "Process name or ID is empty.";
        }
        return commandService.terminateProcess(processNameOrId);
    }
}
