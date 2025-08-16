package com.brotherc.documentcenter.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PasswordUtil {

    @Value("${password.salt}")
    private String salt;

    /**
     * 使用SHA-256加密密码（比MD5更安全）
     * 使用Apache Commons Codec提供的DigestUtils工具类
     *
     * Apache Commons Codec支持的其他算法：
     * - DigestUtils.md5Hex() - MD5算法
     * - DigestUtils.sha1Hex() - SHA-1算法
     * - DigestUtils.sha256Hex() - SHA-256算法（推荐）
     * - DigestUtils.sha384Hex() - SHA-384算法
     * - DigestUtils.sha512Hex() - SHA-512算法
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    public String encryptPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }

        // 将密码和盐值组合，使用Apache Commons Codec进行SHA-256加密
        String saltedPassword = password + salt;
        return DigestUtils.sha256Hex(saltedPassword);
    }

    /**
     * 验证密码
     *
     * @param rawPassword     原始密码
     * @param encryptedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean verifyPassword(String rawPassword, String encryptedPassword) {
        if (rawPassword == null || encryptedPassword == null) {
            return false;
        }
        
        String encrypted = encryptPassword(rawPassword);
        return encrypted.equals(encryptedPassword);
    }

}
