package com.sansim.app.update

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun UpdateDialog(
    currentVersion: String,
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFF2F3F7),
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF34C759)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↑", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("发现新版本", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text("v${updateInfo.versionName}", fontSize = 13.sp, color = Color(0xFF007AFF))
                    }
                }

                // Changelog
                Column(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    Text("更新内容", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier.fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = formatChangelog(updateInfo.changelog),
                            fontSize = 14.sp,
                            color = Color(0xFF4B5563),
                            lineHeight = 21.sp
                        )
                    }
                }

                // Current version
                Text(
                    "当前版本：v$currentVersion",
                    fontSize = 12.sp,
                    color = Color(0xFF8A94A6),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Buttons
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(16.dp))
                            .background(Color.White).clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("稍后再说", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF007AFF))
                    }
                    Box(
                        Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF007AFF)).clickable {
                                uriHandler.openUri(updateInfo.downloadUrl)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("立即更新", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}

private fun formatChangelog(raw: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = raw.lines()
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("### ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)) {
                        append(trimmed.removePrefix("### "))
                    }
                    append("\n")
                }
                trimmed.startsWith("## ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                        append(trimmed.removePrefix("## "))
                    }
                    append("\n")
                }
                trimmed.startsWith("# ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                        append(trimmed.removePrefix("# "))
                    }
                    append("\n")
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    append("  •  ")
                    appendInlineMarkdown(trimmed.drop(2))
                    append("\n")
                }
                trimmed.matches(Regex("^\\d+\\..*")) -> {
                    append("  ")
                    appendInlineMarkdown(trimmed)
                    append("\n")
                }
                trimmed.isNotBlank() -> {
                    appendInlineMarkdown(trimmed)
                    append("\n")
                }
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(text: String) {
    val boldRegex = Regex("\\*\\*(.+?)\\*\\*")
    var last = 0
    for (match in boldRegex.findAll(text)) {
        append(text.substring(last, match.range.first))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(match.groupValues[1])
        }
        last = match.range.last + 1
    }
    if (last < text.length) {
        append(text.substring(last))
    }
}

