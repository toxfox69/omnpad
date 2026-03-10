package com.energenai.omnpad.data

enum class FileCategory {
    TEXT,        // Plain text, editable
    CODE,        // Code with syntax highlighting, editable
    MARKDOWN,    // Markdown with preview toggle
    DATA,        // JSON, XML, YAML, CSV — structured view
    PDF,         // PDF renderer
    OFFICE,      // DOCX, XLSX, PPTX
    IMAGE,       // PNG, JPG, GIF, SVG, WEBP
    AUDIO,       // MP3, WAV, OGG, FLAC
    VIDEO,       // MP4, MKV, WEBM
    ARCHIVE,     // ZIP, TAR, GZ — list contents
    BINARY,      // Hex viewer
}

data class FileType(
    val extension: String,
    val category: FileCategory,
    val language: String? = null,  // For syntax highlighting
    val mimeType: String = "application/octet-stream",
)

object FileTypes {
    private val types = mapOf(
        // Text
        "txt" to FileType("txt", FileCategory.TEXT, mimeType = "text/plain"),
        "log" to FileType("log", FileCategory.TEXT, mimeType = "text/plain"),
        "rtf" to FileType("rtf", FileCategory.TEXT, mimeType = "text/rtf"),
        "cfg" to FileType("cfg", FileCategory.TEXT),
        "conf" to FileType("conf", FileCategory.TEXT),
        "ini" to FileType("ini", FileCategory.TEXT),
        "env" to FileType("env", FileCategory.TEXT),
        "properties" to FileType("properties", FileCategory.TEXT),

        // Code
        "py" to FileType("py", FileCategory.CODE, "python"),
        "js" to FileType("js", FileCategory.CODE, "javascript"),
        "ts" to FileType("ts", FileCategory.CODE, "typescript"),
        "jsx" to FileType("jsx", FileCategory.CODE, "javascript"),
        "tsx" to FileType("tsx", FileCategory.CODE, "typescript"),
        "java" to FileType("java", FileCategory.CODE, "java"),
        "kt" to FileType("kt", FileCategory.CODE, "kotlin"),
        "kts" to FileType("kts", FileCategory.CODE, "kotlin"),
        "c" to FileType("c", FileCategory.CODE, "c"),
        "cpp" to FileType("cpp", FileCategory.CODE, "cpp"),
        "cc" to FileType("cc", FileCategory.CODE, "cpp"),
        "h" to FileType("h", FileCategory.CODE, "c"),
        "hpp" to FileType("hpp", FileCategory.CODE, "cpp"),
        "cs" to FileType("cs", FileCategory.CODE, "csharp"),
        "go" to FileType("go", FileCategory.CODE, "go"),
        "rs" to FileType("rs", FileCategory.CODE, "rust"),
        "rb" to FileType("rb", FileCategory.CODE, "ruby"),
        "php" to FileType("php", FileCategory.CODE, "php"),
        "swift" to FileType("swift", FileCategory.CODE, "swift"),
        "dart" to FileType("dart", FileCategory.CODE, "dart"),
        "lua" to FileType("lua", FileCategory.CODE, "lua"),
        "r" to FileType("r", FileCategory.CODE, "r"),
        "pl" to FileType("pl", FileCategory.CODE, "perl"),
        "sh" to FileType("sh", FileCategory.CODE, "bash"),
        "bash" to FileType("bash", FileCategory.CODE, "bash"),
        "zsh" to FileType("zsh", FileCategory.CODE, "bash"),
        "fish" to FileType("fish", FileCategory.CODE, "bash"),
        "bat" to FileType("bat", FileCategory.CODE, "batch"),
        "cmd" to FileType("cmd", FileCategory.CODE, "batch"),
        "ps1" to FileType("ps1", FileCategory.CODE, "powershell"),
        "sql" to FileType("sql", FileCategory.CODE, "sql"),
        "html" to FileType("html", FileCategory.CODE, "html"),
        "htm" to FileType("htm", FileCategory.CODE, "html"),
        "css" to FileType("css", FileCategory.CODE, "css"),
        "scss" to FileType("scss", FileCategory.CODE, "css"),
        "less" to FileType("less", FileCategory.CODE, "css"),
        "vue" to FileType("vue", FileCategory.CODE, "html"),
        "svelte" to FileType("svelte", FileCategory.CODE, "html"),
        "gradle" to FileType("gradle", FileCategory.CODE, "groovy"),
        "groovy" to FileType("groovy", FileCategory.CODE, "groovy"),
        "scala" to FileType("scala", FileCategory.CODE, "scala"),
        "zig" to FileType("zig", FileCategory.CODE, "zig"),
        "nim" to FileType("nim", FileCategory.CODE, "nim"),
        "ex" to FileType("ex", FileCategory.CODE, "elixir"),
        "exs" to FileType("exs", FileCategory.CODE, "elixir"),
        "erl" to FileType("erl", FileCategory.CODE, "erlang"),
        "hs" to FileType("hs", FileCategory.CODE, "haskell"),
        "ml" to FileType("ml", FileCategory.CODE, "ocaml"),
        "clj" to FileType("clj", FileCategory.CODE, "clojure"),
        "v" to FileType("v", FileCategory.CODE, "verilog"),
        "vhd" to FileType("vhd", FileCategory.CODE, "vhdl"),
        "asm" to FileType("asm", FileCategory.CODE, "asm"),
        "s" to FileType("s", FileCategory.CODE, "asm"),
        "proto" to FileType("proto", FileCategory.CODE, "protobuf"),
        "tf" to FileType("tf", FileCategory.CODE, "hcl"),
        "hcl" to FileType("hcl", FileCategory.CODE, "hcl"),

        // Config-as-code (editable but no highlighting needed beyond keyword)
        "dockerfile" to FileType("dockerfile", FileCategory.CODE, "docker"),
        "makefile" to FileType("makefile", FileCategory.CODE, "makefile"),
        "cmake" to FileType("cmake", FileCategory.CODE, "cmake"),
        "gitignore" to FileType("gitignore", FileCategory.TEXT),
        "dockerignore" to FileType("dockerignore", FileCategory.TEXT),
        "editorconfig" to FileType("editorconfig", FileCategory.TEXT),

        // Markdown
        "md" to FileType("md", FileCategory.MARKDOWN),
        "markdown" to FileType("markdown", FileCategory.MARKDOWN),
        "mdx" to FileType("mdx", FileCategory.MARKDOWN),
        "rst" to FileType("rst", FileCategory.TEXT),  // reStructuredText as plain text

        // Structured data
        "json" to FileType("json", FileCategory.DATA, "json"),
        "jsonl" to FileType("jsonl", FileCategory.DATA, "json"),
        "xml" to FileType("xml", FileCategory.DATA, "xml"),
        "yaml" to FileType("yaml", FileCategory.DATA, "yaml"),
        "yml" to FileType("yml", FileCategory.DATA, "yaml"),
        "toml" to FileType("toml", FileCategory.DATA, "toml"),
        "csv" to FileType("csv", FileCategory.DATA),
        "tsv" to FileType("tsv", FileCategory.DATA),
        "plist" to FileType("plist", FileCategory.DATA, "xml"),

        // PDF
        "pdf" to FileType("pdf", FileCategory.PDF, mimeType = "application/pdf"),

        // Office
        "docx" to FileType("docx", FileCategory.OFFICE, mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        "doc" to FileType("doc", FileCategory.OFFICE, mimeType = "application/msword"),
        "xlsx" to FileType("xlsx", FileCategory.OFFICE, mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        "xls" to FileType("xls", FileCategory.OFFICE, mimeType = "application/vnd.ms-excel"),
        "pptx" to FileType("pptx", FileCategory.OFFICE, mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
        "odt" to FileType("odt", FileCategory.OFFICE),
        "ods" to FileType("ods", FileCategory.OFFICE),

        // Images
        "png" to FileType("png", FileCategory.IMAGE, mimeType = "image/png"),
        "jpg" to FileType("jpg", FileCategory.IMAGE, mimeType = "image/jpeg"),
        "jpeg" to FileType("jpeg", FileCategory.IMAGE, mimeType = "image/jpeg"),
        "gif" to FileType("gif", FileCategory.IMAGE, mimeType = "image/gif"),
        "bmp" to FileType("bmp", FileCategory.IMAGE, mimeType = "image/bmp"),
        "webp" to FileType("webp", FileCategory.IMAGE, mimeType = "image/webp"),
        "svg" to FileType("svg", FileCategory.IMAGE, mimeType = "image/svg+xml"),
        "ico" to FileType("ico", FileCategory.IMAGE, mimeType = "image/x-icon"),
        "tiff" to FileType("tiff", FileCategory.IMAGE, mimeType = "image/tiff"),
        "tif" to FileType("tif", FileCategory.IMAGE, mimeType = "image/tiff"),
        "heic" to FileType("heic", FileCategory.IMAGE, mimeType = "image/heic"),
        "avif" to FileType("avif", FileCategory.IMAGE, mimeType = "image/avif"),

        // Audio
        "mp3" to FileType("mp3", FileCategory.AUDIO, mimeType = "audio/mpeg"),
        "wav" to FileType("wav", FileCategory.AUDIO, mimeType = "audio/wav"),
        "ogg" to FileType("ogg", FileCategory.AUDIO, mimeType = "audio/ogg"),
        "flac" to FileType("flac", FileCategory.AUDIO, mimeType = "audio/flac"),
        "aac" to FileType("aac", FileCategory.AUDIO, mimeType = "audio/aac"),
        "m4a" to FileType("m4a", FileCategory.AUDIO, mimeType = "audio/mp4"),
        "wma" to FileType("wma", FileCategory.AUDIO),
        "opus" to FileType("opus", FileCategory.AUDIO, mimeType = "audio/opus"),

        // Video
        "mp4" to FileType("mp4", FileCategory.VIDEO, mimeType = "video/mp4"),
        "mkv" to FileType("mkv", FileCategory.VIDEO, mimeType = "video/x-matroska"),
        "webm" to FileType("webm", FileCategory.VIDEO, mimeType = "video/webm"),
        "avi" to FileType("avi", FileCategory.VIDEO, mimeType = "video/x-msvideo"),
        "mov" to FileType("mov", FileCategory.VIDEO, mimeType = "video/quicktime"),
        "wmv" to FileType("wmv", FileCategory.VIDEO),
        "flv" to FileType("flv", FileCategory.VIDEO),
        "m4v" to FileType("m4v", FileCategory.VIDEO),

        // Archives
        "zip" to FileType("zip", FileCategory.ARCHIVE, mimeType = "application/zip"),
        "tar" to FileType("tar", FileCategory.ARCHIVE),
        "gz" to FileType("gz", FileCategory.ARCHIVE, mimeType = "application/gzip"),
        "tgz" to FileType("tgz", FileCategory.ARCHIVE),
        "bz2" to FileType("bz2", FileCategory.ARCHIVE),
        "xz" to FileType("xz", FileCategory.ARCHIVE),
        "7z" to FileType("7z", FileCategory.ARCHIVE),
        "rar" to FileType("rar", FileCategory.ARCHIVE),
        "jar" to FileType("jar", FileCategory.ARCHIVE, mimeType = "application/java-archive"),
        "apk" to FileType("apk", FileCategory.ARCHIVE),
        "aar" to FileType("aar", FileCategory.ARCHIVE),
        "war" to FileType("war", FileCategory.ARCHIVE),
        "whl" to FileType("whl", FileCategory.ARCHIVE),
    )

    fun detect(fileName: String): FileType {
        val name = fileName.lowercase()
        // Handle files with no extension (Dockerfile, Makefile, etc.)
        val baseName = name.substringAfterLast("/").substringAfterLast("\\")
        when (baseName) {
            "dockerfile" -> return types["dockerfile"]!!
            "makefile" -> return types["makefile"]!!
            "cmakelists.txt" -> return types["cmake"]!!
            ".gitignore" -> return types["gitignore"]!!
            ".dockerignore" -> return types["dockerignore"]!!
            ".editorconfig" -> return types["editorconfig"]!!
            ".env" -> return types["env"]!!
        }

        val ext = name.substringAfterLast('.', "").lowercase()
        return types[ext] ?: FileType(ext, guessCategory(ext))
    }

    private fun guessCategory(ext: String): FileCategory {
        // Try to detect if it's text by extension pattern
        return FileCategory.BINARY
    }
}
