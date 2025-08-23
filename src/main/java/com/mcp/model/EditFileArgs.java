package com.mcp.model;

import java.util.List;

public record EditFileArgs(String path, List<Edit> edits, Boolean dryRun) {
}
