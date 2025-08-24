package com.mcp.model;

import lombok.Getter;

import java.util.List;

@Getter
public class EditResult {
    String error;
    List<String> originalLines;
    List<String> modifiedLines;
    String modifiedContent;

    public EditResult(List<String> originalLines, List<String> modifiedLines, String modifiedContent, String error) {
        this.originalLines = originalLines;
        this.modifiedLines = modifiedLines;
        this.modifiedContent = modifiedContent;
        this.error = error;
    }
}
