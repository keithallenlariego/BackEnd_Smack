package com.example.securewebchatroom.service;

import com.example.securewebchatroom.enums.FlagReason;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

// A simple keyword-based "AI" moderator. It is a starting point: to use a real model (for example an
// LLM or a trained classifier), replace the body of analyze() and keep the same return type.
@Component
public class ContentAnalyzer {

    public record Detection(FlagReason reason, double confidence) {}

    private static final List<String> SPAM_WORDS = List.of(
            "free gift", "gift card", "click here", "click this", "you won", "winner", "prize",
            "limited offer", "buy now", "earn money", "crypto", "giveaway");
    private static final List<String> ABUSIVE_WORDS = List.of(
            "idiot", "stupid", "useless", "shut up", "hate you", "loser", "moron", "dumb", "get out of here");
    private static final Pattern LINK = Pattern.compile("(https?://|www\\.)\\S+", Pattern.CASE_INSENSITIVE);

    // Returns null when the message looks fine.
    public Detection analyze(String text) {
        String lower = text.toLowerCase();
        int abusiveHits = count(lower, ABUSIVE_WORDS);
        int spamHits = count(lower, SPAM_WORDS) + (LINK.matcher(text).find() ? 1 : 0);

        if (abusiveHits > 0 && abusiveHits >= spamHits) {
            return new Detection(FlagReason.ABUSIVE, Math.min(0.98, 0.70 + 0.10 * abusiveHits));
        }
        if (spamHits > 0 && (spamHits >= 2 || count(lower, SPAM_WORDS) > 0)) {
            return new Detection(FlagReason.SPAM, Math.min(0.98, 0.65 + 0.10 * spamHits));
        }
        return null;
    }

    private int count(String text, List<String> words) {
        int hits = 0;
        for (String word : words) {
            if (text.contains(word)) hits++;
        }
        return hits;
    }
}
