package dev.motions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads server messages such as "Steve paid you $500M" or "You have been paid $500M by .Bedrock Player".
 * Supports Java names and Bedrock (Floodgate) names that start with a "." and may contain spaces.
 */
final class PaymentParser {
    private PaymentParser() {}

    /** Result of the last successful parse() (static so reading chat allocates almost nothing). */
    static String name;  // null if the message didn't name anybody
    static long amount;

    // Bedrock: "." + gamertag (up to 2 extra words, never "has/just/paid/payed/you"). Java: 3-16 chars, not a bare amount.
    private static final String NAME = "(\\.[A-Za-z0-9_]{1,16}(?:\\s(?!(?:has|just|paid|payed|you)\\b)[A-Za-z0-9_]{1,16}){0,2}"
            + "|(?!\\d+[kmbt]?\\b)[A-Za-z0-9_]{3,16})";
    private static final int CI = Pattern.CASE_INSENSITIVE;
    private static final Pattern PAID_YOU = Pattern.compile(NAME + "\\s+(?:(?:has|just)\\s+)?(?:paid|payed)\\s+you", CI);
    private static final Pattern BEEN_PAID = Pattern.compile("\\byou\\s+(?:(?:have|'ve)\\s+)?(?:been\\s+)?(?:paid|payed)\\b", CI);
    private static final Pattern BY_USER = Pattern.compile("\\b(?:by|from)\\s+" + NAME, CI);
    private static final Pattern AMOUNT = Pattern.compile("(?<![A-Za-z0-9_.])\\$?(\\d[\\d,]*(?:\\.\\d+)?)\\s*([kmbt])?(?![A-Za-z])", CI);
    // normal player chat ("Name: text", "<Name> text", "Name » text") is ignored so it can't be faked
    private static final Pattern CHAT_LIKE = Pattern.compile("^\\s*(?:<[^>]{1,40}>|(?:\\[[^\\]]{1,20}\\]\\s*)*\\.?[A-Za-z0-9_]{3,16}\\s*(?::|\u00bb|>>))");

    /** Cheap scan so ordinary chat never reaches the regexes. */
    private static boolean mentionsPay(String s) {
        for (int i = 0, n = s.length() - 3; i < n; i++) {
            if (s.regionMatches(true, i, "paid", 0, 4) || s.regionMatches(true, i, "paye", 0, 4)) return true;
        }
        return false;
    }

    /** Returns true if the message is a payment; the result is in {@link #name} and {@link #amount}. */
    static boolean parse(String msg) {
        if (msg == null || msg.length() > 300 || !mentionsPay(msg)) return false;
        if (CHAT_LIKE.matcher(msg).find()) return false;

        String user = null, rest;
        Matcher m = PAID_YOU.matcher(msg);
        if (m.find()) {
            user = m.group(1);
            rest = msg.substring(m.end());
        } else if (BEEN_PAID.matcher(msg).find()) {
            Matcher b = BY_USER.matcher(msg);
            if (b.find()) user = b.group(1);
            rest = msg;
        } else {
            return false;
        }
        Matcher a = AMOUNT.matcher(rest);
        if (!a.find()) return false;
        long v = AuctionManager.parsePrice(a.group(1) + (a.group(2) == null ? "" : a.group(2)));
        if (v <= 0) return false;
        name = user;
        amount = v;
        return true;
    }
}
