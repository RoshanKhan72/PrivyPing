package com.example.privyping.ui.analysis

import com.example.privyping.ui.model.NotificationItem
import java.util.regex.Pattern

/**
 * Rule-based analysis engine (offline, explainable).
 */
object AnalysisEngine {

    data class AnalysisResult(
        val riskLevel: RiskLevel,
        val confidence: Int,
        val summary: String
    )

    private val URL_PATTERN = Pattern.compile(
        "(https?://|www\\.)[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,}(/\\S*)?",
        Pattern.CASE_INSENSITIVE
    )

    fun analyze(item: NotificationItem): AnalysisResult {

        val text = item.message.lowercase()
        var score = 0
        val reasons = mutableListOf<String>()

        // 🔗 Link Analysis
        val links = extractLinks(item.message)
        if (links.isNotEmpty()) {
            score += 20
            reasons.add("Contains ${links.size} link(s)")
            
            links.forEach { link ->
                val linkLower = link.lowercase()
                
                // 🔴 High Risk Link Indicators
                if (linkLower.startsWith("http://")) {
                    score += 15
                    reasons.add("Uses insecure link (HTTP)")
                }
                
                val shorteners = listOf("bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly", "is.gd", "buff.ly")
                if (shorteners.any { linkLower.contains(it) }) {
                    score += 25
                    reasons.add("Uses a URL shortener which can hide malicious sites")
                }
                
                // Check for IP address instead of domain
                val ipPattern = Pattern.compile("https?://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")
                if (ipPattern.matcher(linkLower).find()) {
                    score += 30
                    reasons.add("Link uses an IP address instead of a domain name (highly suspicious)")
                }
                
                // Suspicious keywords in URL
                val suspiciousUrlKeywords = listOf("login", "verify", "account", "update", "banking", "secure", "signin")
                if (suspiciousUrlKeywords.any { linkLower.contains(it) }) {
                    score += 20
                    reasons.add("Link contains suspicious keywords like 'login' or 'verify'")
                }
            }
        }

        // 🔴 Urgency / pressure
        val urgencyWords = listOf("urgent", "immediately", "act now", "expires today", "blocked")
        if (urgencyWords.any { text.contains(it) }) {
            score += 30
            reasons.add("Uses urgency or pressure tactics")
        }

        // 💰 Money / reward
        val moneyWords = listOf("won", "prize", "lottery", "reward", "₹", "rs", "cash")
        if (moneyWords.any { text.contains(it) }) {
            score += 25
            reasons.add("Mentions money or rewards")
        }

        // ⚠️ Threats
        val threatWords = listOf("blocked", "suspended", "closed", "legal action")
        if (threatWords.any { text.contains(it) }) {
            score += 25
            reasons.add("Threatens negative consequences")
        }

        // 🔗 Action requests
        val actionWords = listOf("click", "verify", "confirm", "login", "update")
        if (actionWords.any { text.contains(it) }) {
            score += 20
            reasons.add("Asks for immediate action")
        }

        // ✅ Trusted apps reduce risk
        val trustedApps = listOf("whatsapp", "gmail", "google", "paytm", "phonepe")
        if (trustedApps.any { item.appName.lowercase().contains(it) }) {
            score -= 15
            reasons.add("Message comes from a commonly trusted app")
        }

        // 🟢 Informational tone reduces risk
        val safeWords = listOf("thank you", "successfully", "completed", "received")
        if (safeWords.any { text.contains(it) }) {
            score -= 20
            reasons.add("Uses informational or neutral language")
        }

        score = score.coerceIn(0, 100)

        val riskLevel = when {
            score >= 70 -> RiskLevel.HIGH
            score >= 35 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        val summary = when (riskLevel) {
            RiskLevel.HIGH ->
                "This message shows strong scam indicators. ${reasons.joinToString(", ")}."

            RiskLevel.MEDIUM ->
                "This message has some suspicious patterns. ${reasons.joinToString(", ")}."

            RiskLevel.LOW ->
                "This message appears safe. ${
                    reasons.ifEmpty { listOf("No suspicious patterns detected") }
                        .joinToString(", ")
                }."
        }

        return AnalysisResult(
            riskLevel = riskLevel,
            confidence = score,
            summary = summary
        )
    }

    private fun extractLinks(text: String): List<String> {
        val links = mutableListOf<String>()
        val matcher = URL_PATTERN.matcher(text)
        while (matcher.find()) {
            links.add(matcher.group())
        }
        return links
    }
}
