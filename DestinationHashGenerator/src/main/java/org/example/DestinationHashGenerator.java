package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class DestinationHashGenerator {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar DestinationHashGenerator.jar <PRN> <path to json file>");
            return;
        }

        String prnNumber = args[0].toLowerCase().replace(" ", "");
        String jsonFilePath = args[1];

        try {
            // Parse JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));

            // Find the first occurrence of the key "destination"
            String destinationValue = findDestinationValue(rootNode);
            if (destinationValue == null) {
                System.err.println("No 'destination' key found in the JSON file.");
                return;
            }

            // Generate a random 8-character alphanumeric string
            String randomString = generateRandomString(8);

            // Concatenate PRN Number, destination value, and random string
            String concatenatedValue = prnNumber + destinationValue + randomString;

            // Generate MD5 hash
            String md5Hash = generateMD5Hash(concatenatedValue);

            // Output the result in the format <hash>;<random string>
            System.out.println(md5Hash + ";" + randomString);

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    @org.jetbrains.annotations.Nullable
    private static String findDestinationValue(JsonNode node) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if ("destination".equals(field.getKey())) {
                    return field.getValue().asText();
                } else {
                    String result = findDestinationValue(field.getValue());
                    if (result != null) {
                        return result;
                    }
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                String result = findDestinationValue(arrayItem);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private static String generateMD5Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : messageDigest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
