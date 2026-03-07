package com.utils;

public class Protocol {

    // 📤 Commandes envoyées par le CLIENT vers le SERVEUR
    public static final String REGISTER    = "REGISTER";     // REGISTER|username|password
    public static final String LOGIN       = "LOGIN";        // LOGIN|username|password
    public static final String LOGOUT      = "LOGOUT";       // LOGOUT|username
    public static final String SEND_MSG    = "SEND_MSG";     // SEND_MSG|sender|receiver|contenu
    public static final String GET_USERS   = "GET_USERS";    // GET_USERS
    public static final String GET_HISTORY = "GET_HISTORY";  // GET_HISTORY|user1|user2

    // 📥 Réponses envoyées par le SERVEUR vers le CLIENT
    public static final String SUCCESS     = "SUCCESS";      // SUCCESS|message
    public static final String ERROR       = "ERROR";        // ERROR|message
    public static final String NEW_MSG     = "NEW_MSG";      // NEW_MSG|sender|contenu|dateEnvoi
    public static final String USER_LIST   = "USER_LIST";    // USER_LIST|user1|user2|user3...
    public static final String HISTORY_MSG = "HISTORY_MSG";  // HISTORY_MSG|sender|receiver|contenu|date|statut

    // Séparateur
    public static final String SEP = "|";
}