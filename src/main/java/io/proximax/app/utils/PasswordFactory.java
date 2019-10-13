package io.proximax.app.utils;

import io.proximax.app.db.PasswordAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author thcao
 */
public class PasswordFactory {

    public static String generatePassword(PasswordAttributes attributes, int length) {
        if (attributes.attributesAreNotEmpty()) {
            Random random = new Random();
            StringBuilder builder = new StringBuilder();
            while (builder.length() < length) {
                switch (random.nextInt(4)) {
                    case 0: {
                        if (attributes.hasCapitalLetters()) {
                            builder.append((char) (65 + random.nextInt(26)));
                        }
                        break;
                    }
                    case 1: {
                        if (attributes.hasSmallLetters()) {
                            builder.append((char) (97 + random.nextInt(26)));
                        }
                        break;
                    }
                    case 2: {
                        if (attributes.hasNumbers()) {
                            builder.append((random.nextInt(10)));
                        }
                        break;
                    }
                    case 3: {
                        if (attributes.hasSymbols()) {
                            builder.append((char) (33 + random.nextInt(15)));
                        }
                        break;
                    }
                }
            }
            return builder.toString();
        } else {
            return generateSimplePassword(length);
        }

    }

    public static String generateSimplePassword(int length) {
        int count = 0;
        int i;
        char[] englishCharStr = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', /*'l',*/ 'm', 'n', /*'o',*/ 'p', 'q', 'r', 's', 't', /*'u', 'v',*/ 'w', 'x', 'y', 'z'};
        char[] numberCharStr = { /*'0', '1',*/'2', '3', '4', '5', '6', '7', '8', '9'};
        char[] str = new char[0];
        boolean isUseEnglishCharStr = true;
        boolean isUseNumberCharStr = true;

        if (isUseEnglishCharStr) {
            int oldLength = str.length;
            char[] tempCharStr = str;
            str = new char[oldLength + englishCharStr.length];
            for (int j = 0; j < tempCharStr.length; j++) {
                str[j] = tempCharStr[j];
            }
            for (int j = 0; j < englishCharStr.length; j++) {
                str[oldLength + j] = englishCharStr[j];
            }
        }
        if (isUseNumberCharStr) {
            int oldLength = str.length;
            char[] tempCharStr = str;
            str = new char[oldLength + numberCharStr.length];
            for (int j = 0; j < tempCharStr.length; j++) {
                str[j] = tempCharStr[j];
            }
            for (int j = 0; j < numberCharStr.length; j++) {
                str[oldLength + j] = numberCharStr[j];
            }
        }
        final int maxNum = str.length;
        StringBuilder pwd = new StringBuilder();
        Random r = new Random();
        while (count < length) {
            i = Math.abs(r.nextInt(maxNum));
            if (i >= 0 && i < str.length) {
                pwd.append(str[i]);
                count++;
            }
        }
        return pwd.toString();
    }

    public static List<String> generateMultiplePasswords(PasswordAttributes attributes, int length, int quantity) {
        List<String> result = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            result.add(generatePassword(attributes, length));
        }
        return result;
    }

}
