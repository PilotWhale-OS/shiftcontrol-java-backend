package at.shiftcontrol.shiftservice.util;

import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import at.shiftcontrol.lib.type.SocialMediaLinkType;
import at.shiftcontrol.shiftservice.dto.event.SocialMediaLinkDto;

public class SocialLinksParser {
    private SocialLinksParser() {
    }

    private static final Pattern SPLIT = Pattern.compile("\\s*,\\s*");

    private static final Map<SocialMediaLinkType, List<String>> HOST_MATCHERS = Map.of(
        SocialMediaLinkType.INSTAGRAM, List.of("instagram.com"),
        SocialMediaLinkType.FACEBOOK, List.of("facebook.com", "fb.com"),
        SocialMediaLinkType.X, List.of("x.com", "twitter.com"),
        SocialMediaLinkType.TIKTOK, List.of("tiktok.com"),
        SocialMediaLinkType.YOUTUBE, List.of("youtube.com", "youtu.be"),
        SocialMediaLinkType.LINKEDIN, List.of("linkedin.com"),
        SocialMediaLinkType.TWITCH, List.of("twitch.tv"),
        SocialMediaLinkType.DISCORD, List.of("discord.gg", "discord.com"),
        SocialMediaLinkType.REDDIT, List.of("reddit.com", "redd.it")
    );

    public static List<SocialMediaLinkDto> parseToDtos(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }

        var parsed = new ArrayList<SocialMediaLinkDto>();
        for (String token : SPLIT.split(csv.trim())) {
            if (token == null || token.isBlank()) {
                continue;
            }

            String normalizedUrl = normalizeUrl(token);
            SocialMediaLinkType type = inferType(normalizedUrl);

            parsed.add(SocialMediaLinkDto.builder()
                .type(type)
                .url(normalizedUrl)
                .build());
        }

        return dedupeByKey(parsed);
    }

    private static List<SocialMediaLinkDto> dedupeByKey(List<SocialMediaLinkDto> input) {
        var seen = new HashSet<String>();
        var out = new ArrayList<SocialMediaLinkDto>();

        for (SocialMediaLinkDto dto : input) {
            String key = dto.createKey();
            if (seen.add(key)) {
                out.add(dto);
            }
        }

        return out;
    }

    private static String normalizeUrl(String input) {
        String trimmed = input.trim();

        // Allow users to paste "instagram.com/xyz" without scheme
        if (!trimmed.contains("://")) {
            trimmed = "https://" + trimmed;
        }

        URI uri = safeParse(trimmed);

        // normalize host to lowercase + ASCII (punycode), remove trailing slash
        String scheme = (uri.getScheme() == null) ? "https" : uri.getScheme().toLowerCase();
        String host = uri.getHost();
        if (host != null) {
            host = IDN.toASCII(host.toLowerCase());
        }

        String path = uri.getRawPath() == null ? "" : uri.getRawPath();
        String query = uri.getRawQuery() == null ? null : uri.getRawQuery();

        // rebuild to normalized form
        URI normalized = safeParse(buildUriString(scheme, host, path, query));

        String s = normalized.toString();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private static String buildUriString(String scheme, String host, String path, String query) {
        StringBuilder b = new StringBuilder();
        b.append(scheme).append("://");
        if (host != null) {
            b.append(host);
        }
        if (path != null && !path.isBlank()) {
            if (!path.startsWith("/")) {
                b.append("/");
            }
            b.append(path);
        }
        if (query != null && !query.isBlank()) {
            b.append("?").append(query);
        }
        return b.toString();
    }

    private static URI safeParse(String s) {
        try {
            return new URI(s);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + s, e);
        }
    }

    private static SocialMediaLinkType inferType(String normalizedUrl) {
        URI uri = safeParse(normalizedUrl);
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return SocialMediaLinkType.OTHER;
        }

        host = host.toLowerCase();
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }

        for (var entry : HOST_MATCHERS.entrySet()) {
            for (String needle : entry.getValue()) {
                if (host.equals(needle) || host.endsWith("." + needle)) {
                    return entry.getKey();
                }
            }
        }

        // If it is a valid URL but not a known social domain, treat it as WEBSITE
        return SocialMediaLinkType.WEBSITE;
    }
}
