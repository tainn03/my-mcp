# Java MCP Filesystem Server

Đây là một Model Context Protocol (MCP) server được triển khai bằng Java, cung cấp khả năng truy cập hệ thống tệp một cách an toàn cho các trợ lý AI. Server này cho phép các mô hình AI đọc, ghi và thao tác với tệp trong các thư mục được chỉ định, đồng thời ngăn chặn truy cập trái phép thông qua xác thực đường dẫn.

## Tính năng

### Thiết kế bảo mật

- **Xác thực đường dẫn**: Tất cả các thao tác với tệp đều bị giới hạn trong các thư mục được cho phép
- **Bảo vệ chống Path Traversal**: Ngăn chặn các tấn công `../` thông qua chuẩn hóa và xác thực đường dẫn
- **Xử lý Symbolic Link**: Xử lý an toàn các symbolic link bằng cách kiểm tra đường dẫn thực của chúng

### Các nhóm công cụ

Server MCP này cung cấp nhiều nhóm công cụ cho nhiều mục đích khác nhau:

#### 1. Công cụ tệp (FileTools)

- **`f01_read_file`**: Đọc nội dung của một tệp
- **`f02_read_multiple_files`**: Đọc nhiều tệp cùng lúc
- **`f03_write_file`**: Tạo mới hoặc ghi đè nội dung vào tệp
- **`f04_move_file`**: Di chuyển hoặc đổi tên tệp và thư mục
- **`f05_get_file_info`**: Lấy thông tin chi tiết về tệp (kích thước, thời gian, quyền)
- **`f06_search_files`**: Tìm kiếm tệp theo glob pattern
- **`f07_edit_file`**: Thực hiện các thay đổi văn bản trong tệp với khả năng xem trước diff
- **`f08_get_changes`**: Lấy diff của các tệp đã thay đổi
- **`f09_search_by_keyword`**: Tìm kiếm tệp có chứa từ khóa cụ thể

#### 2. Công cụ thư mục (DirectoryTools)

- **`d01_create_directory`**: Tạo cấu trúc thư mục mới
- **`d02_list_directory`**: Liệt kê nội dung của thư mục
- **`d03_directory_tree`**: Tạo cây thư mục dưới dạng JSON

#### 3. Công cụ web (WebTools)

- **`w01_search_web`**: Tìm kiếm thông tin trên web
- **`w02_fetch_web_content`**: Lấy nội dung của một trang web
- **`w03_fetch_web_html`**: Lấy mã HTML gốc của trang web
- **`w04_fetch_web_DOM_tree`**: Phân tích cấu trúc của trang web và trả về cây DOM
- **`w05_scrape_web_page_data`**: Scrape dữ liệu cụ thể từ trang web dựa trên CSS selector
- **`w06_generate_web_page_screenshot`**: Tạo ảnh chụp màn hình của một trang web

#### 4. Công cụ lệnh (CommandTools)

- **`c01_run_command`**: Chạy lệnh hệ thống và trả về kết quả
- **`c02_list_processes`**: Liệt kê tất cả các tiến trình đang chạy
- **`c03_terminate_process`**: Tìm và kết thúc một tiến trình theo tên hoặc ID

#### 5. Công cụ cơ sở dữ liệu (DatabaseTools) - Đang phát triển

- **`db01_execute_query`**: Thực thi truy vấn SQL (chưa hoàn thiện)
- **`db02_get_connection`**: Kết nối đến cơ sở dữ liệu (chưa hoàn thiện)
- **`db03_get_databases`**: Lấy danh sách cơ sở dữ liệu (chưa hoàn thiện)
- **`db04_get_schema`**: Lấy schema của cơ sở dữ liệu (chưa hoàn thiện)
- **`db05_get_tables`**: Lấy danh sách bảng trong cơ sở dữ liệu (chưa hoàn thiện)

## Cấu trúc dự án

```
src/
  main/
    java/
      com/
        mcp/
          McpApplication.java       # Điểm khởi đầu ứng dụng
          config/                   # Cấu hình MCP tools
            ToolConfig.java
          model/                    # Các model dữ liệu
            Edit.java
            EditFileArgs.java
            EditResult.java
          service/                  # Logic nghiệp vụ
            CommandService.java
            DirectoryService.java
            FileService.java
            FileVisitorService.java
            FileWatcherService.java
            PathService.java
            WebService.java
            impl/                   # Triển khai các service
          tool/                     # Các công cụ MCP
            CommandTools.java
            DatabaseTools.java
            DirectoryTools.java
            FileTools.java
            WebTools.java
          util/                     # Tiện ích và helpers
            AppendUtils.java
    resources/
      application.yml              # Cấu hình Spring Boot
  test/
    java/
      com/mcp/
        McpApplicationTests.java
build.gradle                       # Cấu hình build
Dockerfile                         # Cấu hình Docker container
```

## Công nghệ sử dụng

- **Ngôn ngữ:** Java 21+
- **Framework:** Spring Boot, Spring AI MCP Server
- **Build tool:** Gradle
- **Dependencies:** Jackson (JSON), Java Diff Utils
- **Đóng gói container:** Docker
- **Registry:** GitHub Container Registry

## Hướng dẫn sử dụng chi tiết

### 1. Yêu cầu hệ thống

- Java 21 hoặc cao hơn
- Gradle (cho build từ source)
- Docker (cho containerization)

### 2. Cài đặt và chạy

#### A. Chạy từ mã nguồn

1. Clone repository:
```bash
git clone https://github.com/tainn03/my-mcp.git
cd my-mcp
```

2. Build project:
```bash
./gradlew clean build -DskipTests
```

3. Chạy ứng dụng:
```bash
java -jar build/libs/mcp-0.0.1-SNAPSHOT.jar /đường/dẫn/đến/thư/mục/được/phép [thêm các thư mục khác...]
```

#### B. Chạy bằng Docker

1. Build Docker image:
```bash
docker build -t ghcr.io/tainn03/my-mcp:latest .
```

2. Chạy container:
```bash
docker run -i --rm --mount type=bind,src=/đường/dẫn/trên/host,dst=/projects/workspace -e ALLOWED_DIRS=/projects/workspace ghcr.io/tainn03/my-mcp:latest
```

### 3. Cấu hình VS Code

Để sử dụng MCP server này với VS Code, tạo hoặc cập nhật file `.vscode/mcp.json`:

1. Với Java, cài đặt như sau:
```json
{
  "servers": {
    "my-mcp-java": {
      "type": "stdio",
      "command": "{JAVA_PATH}",
      "args": [
        "-jar",
        "{JAR_FILE_PATH}"
      ],
      "env": {
        "ALLOWED_DIRS": "YOUR_ALLOWED_DIRS_01,YOUR_ALLOWED_DIRS_02", // Cách nhau dấu phẩy
        "SCREENSHOTS_API_KEY": "YOUR_SCREENSHOTS_API_KEY" // Chỉ dùng cho tool chụp màn hình web
      }
    }
  },
  "inputs": []
}
```
- Ví dụ:
```json
{
  "servers": {
    "my-mcp-java": {
      "type": "stdio",
      "command": "java",
      "args": [
        "-jar",
        "build/libs/mcp-0.0.1-SNAPSHOT.jar"
      ],
      "env": {
        "ALLOWED_DIRS": "${workspaceFolder}",
        "SCREENSHOTS_API_KEY": "YOUR_SCREENSHOTS_API_KEY",
      }
    }
  },
  "inputs": []
}
```

2. Với Docker, cài đặt như sau:
```json
{
  "servers": {
    "my-mcp-docker": {
      "type": "stdio",
      "command": "{DOCKER_PATH}",
      "args": [
        "run",
        "-i",
        "--rm",
        "--mount",
        "type=bind,src={YOUR_ALLOWED_FIRS},dst=/projects",
        "-e",
        "ALLOWED_DIRS={YOUR_ALLOWED_FIRS}",
        "-e",
        "SCREENSHOTS_API_KEY=",
        "ghcr.io/tainn03/my-mcp:latest"
      ]
    }
  },
  "inputs": []
}
```
- Ví dụ: 
```json
{
  "servers": {
    "my-mcp-docker": {
      "type": "stdio",
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "--mount",
        "type=bind,src=${workspaceFolder},dst=/projects/workspace",
        "-e",
        "ALLOWED_DIRS=${workspaceFolder}",
        "-e",
        "SCREENSHOTS_API_KEY=",
        "ghcr.io/tainn03/my-mcp:latest"
      ]
    }
  },
  "inputs": []
}
```

Lưu ý:
- Điều chỉnh đường dẫn Java phù hợp với hệ thống của bạn
- `${workspaceFolder}` sẽ được thay thế bằng đường dẫn đến workspace hiện tại
- Đối với tính năng chụp ảnh màn hình trang web, bạn cần đăng ký API key từ một dịch vụ chụp màn hình web và cung cấp nó trong biến môi trường `SCREENSHOTS_API_KEY`

### 4. Sử dụng các công cụ MCP

#### Ví dụ sử dụng FileTools

Đọc nội dung của một tệp:
```
f01_read_file path="/đường/dẫn/đến/tệp.txt"
```

Tạo hoặc ghi đè một tệp:
```
f03_write_file path="/đường/dẫn/đến/tệp.txt" content="Nội dung muốn viết vào tệp"
```

Tìm kiếm tệp:
```
f06_search_files pattern="*.java" path="/đường/dẫn/bắt/đầu/tìm" excludePatterns=["**/build/**", "**/node_modules/**"]
```

#### Ví dụ sử dụng DirectoryTools

Liệt kê nội dung thư mục:
```
d02_list_directory path="/đường/dẫn/đến/thư/mục"
```

Hiển thị cây thư mục:
```
d03_directory_tree path="/đường/dẫn/đến/thư/mục/gốc"
```

#### Ví dụ sử dụng WebTools

Tìm kiếm thông tin trên web:
```
w01_search_web keyword="Java MCP Server implementation"
```

Chụp ảnh trang web:
```
w06_generate_web_page_screenshot url="https://example.com" fileType="png"
```

#### Ví dụ sử dụng CommandTools

Chạy lệnh hệ thống:
```
c01_run_command command="echo Hello, world!"
```

Liệt kê tiến trình:
```
c02_list_processes
```

### 5. Biến môi trường và cấu hình

| Biến môi trường | Mô tả | Ví dụ |
|-----------------|-------|-------|
| ALLOWED_DIRS | Danh sách các thư mục được phép truy cập (ngăn cách bằng dấu phẩy) | `/home/user/workspace,/tmp/data` |
| SCREENSHOTS_API_KEY | API key cho dịch vụ chụp ảnh màn hình web | `YOUR_API_KEY_HERE` |

## Bảo mật

1. **Kiểm soát truy cập thư mục**: Chỉ các thư mục được chỉ định trong tham số khởi động hoặc biến môi trường mới có thể truy cập
2. **Xác thực đường dẫn**: Mọi đường dẫn đều được xác thực trước khi thực hiện thao tác
3. **Không nâng quyền**: Server chạy với quyền của user khởi động
4. **Xử lý Symbolic Links**: Được giải quyết về đường dẫn thực để ngăn thoát khỏi thư mục được phép

## Xử lý sự cố

### Các lỗi thường gặp

1. **"Access denied" errors**: Đảm bảo đường dẫn nằm trong thư mục được phép
2. **"Path is outside of allowed directories"**: Kiểm tra đã include thư mục cha trong tham số server
3. **"Server không khởi động"**: Xác minh Java 21+ đã được cài đặt và có trong PATH
4. **"Screenshot generation failed"**: Kiểm tra API key và kết nối internet

## Tác giả

[Nguyễn Nhất Tài](https://www.linkedin.com/in/nguyen-nhat-tai-b5217b36a/)