package com.energenai.omnpad.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.energenai.omnpad.ui.theme.*

data class SyntaxRule(
    val pattern: Regex,
    val color: Color,
    val bold: Boolean = false,
    val italic: Boolean = false,
)

object SyntaxHighlighter {

    private val commonRules = listOf(
        // Strings (double-quoted)
        SyntaxRule(Regex("\"(?:[^\"\\\\]|\\\\.)*\""), SynString),
        // Strings (single-quoted)
        SyntaxRule(Regex("'(?:[^'\\\\]|\\\\.)*'"), SynString),
        // Numbers
        SyntaxRule(Regex("\\b\\d+\\.?\\d*([eE][+-]?\\d+)?\\b"), SynNumber),
        // Hex numbers
        SyntaxRule(Regex("\\b0x[0-9a-fA-F]+\\b"), SynNumber),
    )

    private val languageKeywords = mapOf(
        "python" to listOf("def", "class", "import", "from", "return", "if", "elif", "else", "for", "while", "try", "except", "finally", "with", "as", "yield", "lambda", "pass", "break", "continue", "raise", "in", "not", "and", "or", "is", "None", "True", "False", "self", "async", "await", "global", "nonlocal", "assert", "del"),
        "javascript" to listOf("function", "const", "let", "var", "return", "if", "else", "for", "while", "do", "switch", "case", "break", "continue", "try", "catch", "finally", "throw", "new", "this", "class", "extends", "import", "export", "default", "from", "async", "await", "yield", "typeof", "instanceof", "in", "of", "null", "undefined", "true", "false", "void", "delete"),
        "typescript" to listOf("function", "const", "let", "var", "return", "if", "else", "for", "while", "do", "switch", "case", "break", "continue", "try", "catch", "finally", "throw", "new", "this", "class", "extends", "implements", "interface", "type", "enum", "import", "export", "default", "from", "async", "await", "yield", "typeof", "instanceof", "in", "of", "null", "undefined", "true", "false", "void", "delete", "abstract", "readonly", "private", "protected", "public", "static", "as", "is", "keyof", "infer", "never", "unknown", "any"),
        "java" to listOf("public", "private", "protected", "static", "final", "abstract", "class", "interface", "extends", "implements", "import", "package", "return", "if", "else", "for", "while", "do", "switch", "case", "break", "continue", "try", "catch", "finally", "throw", "throws", "new", "this", "super", "void", "int", "long", "double", "float", "boolean", "char", "byte", "short", "null", "true", "false", "instanceof", "synchronized", "volatile", "transient", "native", "enum", "assert", "default", "var", "record", "sealed", "permits", "yield"),
        "kotlin" to listOf("fun", "val", "var", "class", "object", "interface", "abstract", "override", "open", "sealed", "data", "enum", "companion", "import", "package", "return", "if", "else", "when", "for", "while", "do", "try", "catch", "finally", "throw", "is", "in", "as", "by", "init", "constructor", "this", "super", "null", "true", "false", "it", "internal", "private", "protected", "public", "suspend", "inline", "crossinline", "noinline", "reified", "typealias", "lateinit", "lazy"),
        "c" to listOf("auto", "break", "case", "char", "const", "continue", "default", "do", "double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "register", "return", "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while", "NULL", "true", "false", "#include", "#define", "#ifdef", "#ifndef", "#endif", "#pragma"),
        "cpp" to listOf("auto", "break", "case", "catch", "char", "class", "const", "constexpr", "continue", "default", "delete", "do", "double", "else", "enum", "explicit", "extern", "false", "float", "for", "friend", "goto", "if", "inline", "int", "long", "mutable", "namespace", "new", "noexcept", "nullptr", "operator", "override", "private", "protected", "public", "register", "return", "short", "signed", "sizeof", "static", "static_cast", "struct", "switch", "template", "this", "throw", "true", "try", "typedef", "typeid", "typename", "union", "unsigned", "using", "virtual", "void", "volatile", "while", "#include", "#define"),
        "rust" to listOf("fn", "let", "mut", "const", "static", "struct", "enum", "impl", "trait", "type", "use", "mod", "pub", "crate", "self", "super", "return", "if", "else", "match", "for", "while", "loop", "break", "continue", "move", "ref", "as", "in", "where", "async", "await", "unsafe", "extern", "dyn", "box", "true", "false", "Some", "None", "Ok", "Err", "Self"),
        "go" to listOf("break", "case", "chan", "const", "continue", "default", "defer", "else", "fallthrough", "for", "func", "go", "goto", "if", "import", "interface", "map", "package", "range", "return", "select", "struct", "switch", "type", "var", "true", "false", "nil", "iota", "append", "cap", "close", "complex", "copy", "delete", "imag", "len", "make", "new", "panic", "print", "println", "real", "recover"),
        "bash" to listOf("if", "then", "else", "elif", "fi", "for", "while", "do", "done", "case", "esac", "function", "return", "exit", "echo", "read", "local", "export", "source", "set", "unset", "shift", "true", "false", "in", "select", "until", "break", "continue", "declare", "readonly", "trap", "eval", "exec"),
        "sql" to listOf("SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE", "CREATE", "TABLE", "DROP", "ALTER", "ADD", "INDEX", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "ON", "AND", "OR", "NOT", "IN", "BETWEEN", "LIKE", "IS", "NULL", "AS", "ORDER", "BY", "GROUP", "HAVING", "LIMIT", "OFFSET", "UNION", "ALL", "DISTINCT", "COUNT", "SUM", "AVG", "MAX", "MIN", "EXISTS", "CASE", "WHEN", "THEN", "ELSE", "END", "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "CONSTRAINT", "DEFAULT", "VARCHAR", "INT", "INTEGER", "TEXT", "BOOLEAN", "FLOAT", "DOUBLE", "DATE", "TIMESTAMP", "BEGIN", "COMMIT", "ROLLBACK", "TRANSACTION", "VIEW", "TRIGGER", "PROCEDURE", "FUNCTION"),
        "html" to listOf("html", "head", "body", "div", "span", "p", "a", "img", "table", "tr", "td", "th", "ul", "ol", "li", "h1", "h2", "h3", "h4", "h5", "h6", "form", "input", "button", "select", "option", "textarea", "label", "script", "style", "link", "meta", "title", "header", "footer", "nav", "main", "section", "article", "aside"),
        "css" to listOf("color", "background", "margin", "padding", "border", "font", "display", "position", "top", "left", "right", "bottom", "width", "height", "flex", "grid", "align", "justify", "overflow", "opacity", "transform", "transition", "animation", "cursor", "z-index", "box-shadow", "text-align", "line-height", "important"),
    )

    private val commentPatterns = mapOf(
        "python" to listOf(Regex("#[^\n]*")),
        "bash" to listOf(Regex("#[^\n]*")),
        "html" to listOf(Regex("<!--[\\s\\S]*?-->")),
        "css" to listOf(Regex("/\\*[\\s\\S]*?\\*/")),
        "sql" to listOf(Regex("--[^\n]*"), Regex("/\\*[\\s\\S]*?\\*/")),
    )

    // Default comment patterns for C-like languages
    private val cStyleComments = listOf(
        Regex("//[^\n]*"),
        Regex("/\\*[\\s\\S]*?\\*/"),
    )

    fun highlight(text: String, language: String?): AnnotatedString {
        if (language == null || text.length > 100_000) {
            return AnnotatedString(text)
        }

        val builder = AnnotatedString.Builder(text)
        val applied = mutableListOf<IntRange>()

        fun applyIfFree(start: Int, end: Int, style: SpanStyle) {
            if (applied.none { it.first < end && it.last >= start }) {
                builder.addStyle(style, start, end)
                applied.add(start until end)
            }
        }

        // 1. Comments first (highest priority)
        val comments = commentPatterns[language] ?: if (language in setOf(
                "javascript", "typescript", "java", "kotlin", "c", "cpp",
                "csharp", "go", "rust", "swift", "dart", "scala", "groovy",
                "php"
            )
        ) cStyleComments else emptyList()

        for (pattern in comments) {
            for (match in pattern.findAll(text)) {
                applyIfFree(
                    match.range.first, match.range.last + 1,
                    SpanStyle(color = SynComment, fontStyle = FontStyle.Italic)
                )
            }
        }

        // 2. Strings
        for (rule in commonRules.take(2)) {
            for (match in rule.pattern.findAll(text)) {
                applyIfFree(
                    match.range.first, match.range.last + 1,
                    SpanStyle(color = rule.color)
                )
            }
        }

        // 3. Template strings for JS/TS
        if (language in setOf("javascript", "typescript")) {
            val backtick = Regex("`(?:[^`\\\\]|\\\\.)*`")
            for (match in backtick.findAll(text)) {
                applyIfFree(
                    match.range.first, match.range.last + 1,
                    SpanStyle(color = SynString)
                )
            }
        }

        // 4. Keywords
        val keywords = languageKeywords[language]
        if (keywords != null) {
            val kwPattern = if (language == "sql") {
                Regex("\\b(${keywords.joinToString("|")})\\b", RegexOption.IGNORE_CASE)
            } else {
                Regex("\\b(${keywords.joinToString("|")})\\b")
            }
            for (match in kwPattern.findAll(text)) {
                applyIfFree(
                    match.range.first, match.range.last + 1,
                    SpanStyle(color = SynKeyword, fontWeight = FontWeight.Bold)
                )
            }
        }

        // 5. Numbers
        for (rule in commonRules.drop(2)) {
            for (match in rule.pattern.findAll(text)) {
                applyIfFree(
                    match.range.first, match.range.last + 1,
                    SpanStyle(color = rule.color)
                )
            }
        }

        // 6. HTML/XML tags
        if (language in setOf("html", "xml")) {
            val tagPattern = Regex("</?[a-zA-Z][a-zA-Z0-9-]*")
            for (match in tagPattern.findAll(text)) {
                applyIfFree(
                    match.range.first, match.range.last + 1,
                    SpanStyle(color = SynFunction)
                )
            }
            val attrPattern = Regex("\\s([a-zA-Z-]+)=")
            for (match in attrPattern.findAll(text)) {
                val group = match.groups[1] ?: continue
                applyIfFree(
                    group.range.first, group.range.last + 1,
                    SpanStyle(color = SynType)
                )
            }
        }

        return builder.toAnnotatedString()
    }
}
