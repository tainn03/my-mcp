# Java MCP Filesystem Server

A secure Model Context Protocol (MCP) server implementation in Java that provides controlled filesystem access to AI
assistants. This server enables AI models to safely read, write, and manipulate files within specified directories while
preventing unauthorized access through path validation.

## Features

### Security-First Design

- **Path Validation**: All file operations are restricted to explicitly allowed directories
- **Path Traversal Protection**: Prevents `../` attacks through path normalization and validation
- **Symbolic Link Resolution**: Safely handles symbolic links by checking their real paths

### Comprehensive File Operations

The server exposes 11 MCP tools for filesystem manipulation:

- **`read_file`**: Read the complete contents of a single file
- **`read_multiple_files`**: Efficiently read multiple files in one operation
- **`write_file`**: Create new files or overwrite existing ones
- **`edit_file`**: Make line-based edits with diff preview support
- **`create_directory`**: Create single or nested directory structures
- **`list_directory`**: List contents of a directory with type indicators
- **`directory_tree`**: Get a recursive JSON tree view of directories
- **`move_file`**: Move or rename files and directories
- **`search_files`**: Recursively search for files matching patterns
- **`get_file_info`**: Retrieve detailed file metadata (size, timestamps, permissions)
- **`list_allowed_directories`**: Show which directories the server can access

### MCP Resources Support

In addition to tools, the server also supports MCP resources, allowing clients to access file contents through `file://`
URIs.

## Requirements

- Java 21 or higher
- Gradle

## Installation

### Option 1: Download Pre-built JAR

Download the latest release JAR file from the releases page (if available).

### Option 2: Build from Source

1. Clone the repository:

```bash
git clone <repository-url>
cd my-mcp
```

2. Build the project:

```bash
./gradlew clean build -DskipTests
```

This creates a fat JAR with all dependencies at:

```
build/libs/mcp-0.0.1-SNAPSHOT.jar
```

## Usage

### Running the Server

The server requires at least one allowed directory as a command-line argument:

```bash
java -jar build/libs/filesystem-mcp-server-all.jar /path/to/allowed/directory [additional directories...]
```

Example with multiple directories:

```bash
java -jar build/libs/filesystem-mcp-server-all.jar /Users/myuser/documents /Users/myuser/projects
```

### Configuring with Claude Desktop

To use this server with Claude Desktop, add it to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "my-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/mcp-0.0.1-SNAPSHOT.jar",
        "${workspaceFolder}",
        "~/workspace"
      ]
    }
  }
}
```

## Development

### Key Dependencies

- `org.springframework.ai:spring-ai-starter-mcp-server-webmvc` - Spring AI MCP server framework
- `io.github.java-diff-utils:java-diff-utils:4.12` - For generating diffs in edit operations
- `com.fasterxml.jackson.core:jackson-databind:2.15.2` - JSON processing

## Security Considerations

1. **Directory Access**: Only directories explicitly passed as arguments can be accessed
2. **Path Validation**: Every path is validated before any operation
3. **No Elevation**: The server runs with the same permissions as the user who starts it
4. **Symbolic Links**: Resolved to their real paths to prevent escaping allowed directories

## Troubleshooting

### Common Issues

1. **"Access denied" errors**: Ensure the path is within an allowed directory
2. **"Path is outside of allowed directories"**: Check that you've included the parent directory in the server arguments
3. **Server won't start**: Verify Java 21+ is installed and in your PATH

## Author

[Nguyễn Nhất Tài](https://www.linkedin.com/in/nguyen-nhat-tai-b5217b36a/)

## Acknowledgments

- Built with
  the [Spring AI MCP Server](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html) for
  Java
- Inspired by the TypeScript reference implementation