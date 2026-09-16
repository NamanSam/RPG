package com.codequest.content;

import java.math.BigInteger;
import tools.jackson.databind.json.JsonMapper;

public final class AnswerValidator {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private AnswerValidator() {}
    public static boolean matches(String type, String expected, String answer) {
        if (answer == null || answer.length() > 2000) return false;
        try {
            return switch(type) {
                case "OPTION", "TOKEN" -> expected.equals(answer.strip());
                case "OUTPUT" -> normalize(expected).equals(normalize(answer));
                case "INTEGER" -> answer.strip().matches("-?\\d+") && new BigInteger(expected).equals(new BigInteger(answer.strip()));
                case "INT_LIST", "INT_MATRIX" -> {
                    var a = JSON.readTree(answer); var e = JSON.readTree(expected);
                    boolean valid = a.isArray();
                    for (var item : a) {
                        if (type.equals("INT_LIST")) valid &= item.isIntegralNumber();
                        else { valid &= item.isArray(); for (var value : item) valid &= value.isIntegralNumber(); }
                    }
                    yield valid && a.equals(e);
                }
                default -> false;
            };
        } catch (RuntimeException error) { return false; }
    }
    private static String normalize(String text) { return text.replace("\r\n", "\n").strip(); }
}
