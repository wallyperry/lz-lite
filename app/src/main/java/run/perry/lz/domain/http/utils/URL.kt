package run.perry.lz.domain.http.utils

import java.net.URI

/**
 * 从url中抽取出域名+端口
 * https://example.com/path?query=1 -> https://example.com
 */
fun String.extractDomain() = try {
    val uri = URI(this) // 使用 URI 解析
    val scheme = uri.scheme ?: "http" // 默认 http（如果缺少协议）
    val host = uri.host ?: this // 如果解析失败，返回原始字符串
    val port = when (uri.port) {
        -1, 80, 443 -> "" // 标准端口不显示
        else -> ":${uri.port}"
    }
    "$scheme://$host$port"
} catch (_: Exception) {
    this
}

/**
 * 从url中抽取出域名+端口后面的内容
 * https://example.com/path?query=1 -> /path?query=1
 */
fun String.extractPath(): String = try {
    val uri = URI(this)
    buildString {
        append(uri.path.ifEmpty { "/" }) // 确保至少返回 "/"
        uri.query?.let { append("?").append(it) } // 添加查询参数（?a=1&b=2）
        uri.fragment?.let { append("#").append(it) } // 添加片段（#section）
    }
} catch (_: Exception) {
    "/"
}

