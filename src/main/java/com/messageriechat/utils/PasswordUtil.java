package com.utils;


import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // 🔒 Hasher un mot de passe avant de le sauvegarder en base (RG9)
    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // ✅ Vérifier un mot de passe lors de la connexion (RG2)
    public static boolean verify(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}