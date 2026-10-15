package com.youlinbang.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 哈希工具：身份证号只存 SHA-256，用于"一号一实名"唯一约束，不存原文
 */
public final class HashUtil {

    private HashUtil() {
    }

    public static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    /** 身份证掩码：保留前 6 位（地区码）与后 4 位，中间打星 */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 11) {
            return "****";
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }
}
