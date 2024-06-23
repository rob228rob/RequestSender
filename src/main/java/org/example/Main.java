package org.example;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        var roles = ApiService.getRolesGETRequest();
        int randomIndex = getRandomNumber(0, roles.size());

        String role = roles.get(randomIndex);
        String name = getRandomString();
        String surname = getRandomString();
        String email = getRandomEmail();
        String randStatus = "increased";

        var signUpResponse = ApiService.signUpPOSTRequest(surname, name, email, role);
        if (!signUpResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println(signUpResponse.getStatusCode() + " " + signUpResponse.getBody());
            return;
        }
        var code = ApiService.getCodeGETRequest(email);
        code = ValidateAndRemoveQuotes(code);

        ApiService.setStatusPOSTRequest(email, code, randStatus);
    }

    private static String ValidateAndRemoveQuotes(String string) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("String is null or empty");
        }

        if (string.startsWith("\"") && string.endsWith("\"")) {
            return string.substring(1, string.length() - 1);
        }

        return string;
    }

    private static int getRandomNumber(int min, int max) {
        Random rand = new Random();

        return min + rand.nextInt(max - min);
    }

    private static String characters = "qwertyuiopasdfghjklzxcvbnm";

    private static String getRandomString() {
        return getRandomString(10);
    }

    private static String getRandomString(int max) {
        int length = getRandomNumber(3, max);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(getRandomNumber(0, characters.length() - 1)));
        }

        return sb.toString();
    }

    private static String getRandomEmail() {
        String randomString = getRandomString(8);

        return randomString + "@example.ru";
    }

}