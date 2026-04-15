package com.example.mentalhealth.api;

import com.example.mentalhealth.BuildConfig;

/**
 * URL и ключ берутся из {@link BuildConfig} (Gradle: {@code supabase.url} / {@code supabase.anon.key}
 * в {@code local.properties}, иначе — значения по умолчанию из {@code app/build.gradle.kts}).
 * Поддерживаются и JWT {@code eyJ...}, и публичный ключ {@code sb_publishable_...}.
 */
public final class SupabaseConfig {

    private SupabaseConfig() {
    }

    public static String getBaseUrl() {
        String u = BuildConfig.SUPABASE_URL;
        if (u == null) {
            return "";
        }
        u = u.trim();
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }

    public static String getAnonKey() {
        String k = BuildConfig.SUPABASE_ANON_KEY;
        if (k == null) {
            return "";
        }
        return k.trim();
    }

    public static boolean hasValidAnonKey() {
        return !getAnonKey().isEmpty();
    }
}
