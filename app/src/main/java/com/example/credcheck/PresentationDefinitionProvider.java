package com.example.credcheck;

public class PresentationDefinitionProvider {

    public static String getPresentationDefinition(String accountType) {
        switch (accountType.toLowerCase()) {
            case "restaurant":
                return getRestaurantDefinition();
            case "hospital":
                return getHospitalDefinition();
            default:
                return null;
        }
    }

    private static String getRestaurantDefinition() {
        return "{\n" +
                "  \"type\": \"vp_token\",\n" +
                "  \"presentation_definition\": {\n" +
                "    \"id\": \"2ec7e307-81aa-490c-a194-3f22640e919d\",\n" +
                "    \"input_descriptors\": [\n" +
                "      {\n" +
                "        \"id\": \"f3382cfb-e2d8-4bac-abdb-eb91daf65152\",\n" +
                "        \"name\": \"Person Identification Data (PID)\",\n" +
                "        \"purpose\": \"\",\n" +
                "        \"format\": {\n" +
                "          \"vc+sd-jwt\": {\n" +
                "            \"sd-jwt_alg_values\": [\"ES256\", \"ES384\", \"ES512\"],\n" +
                "            \"kb-jwt_alg_values\": [\"RS256\", \"RS384\", \"RS512\", \"ES256\", \"ES384\", \"ES512\"]\n" +
                "          }\n" +
                "        },\n" +
                "        \"constraints\": {\n" +
                "          \"fields\": [\n" +
                "            { \"path\": [\"$.vct\"], \"filter\": {\"type\": \"string\", \"const\": \"urn:eu.europa.ec.eudi:pid:1\"} },\n" +
                "            { \"path\": [\"$.age_equal_or_over.18\"], \"intent_to_retain\": false }\n" +
                "          ]\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"nonce\": \"be95d067-f674-46b5-834e-78633c605e98\",\n" +
                "  \"request_uri_method\": \"get\"\n" +
                "}";
    }

    private static String getHospitalDefinition() {
        return "{\n" +
                "  \"type\": \"vp_token\",\n" +
                "  \"presentation_definition\": {\n" +
                "    \"id\": \"79c1b44a-c1d8-4f54-a54e-7d0a3dc05c7a\",\n" +
                "    \"input_descriptors\": [\n" +
                "      {\n" +
                "        \"id\": \"cbf86843-d9d1-4742-b2c8-cd90faad3c43\",\n" +
                "        \"name\": \"European Health Insurance Card (EHIC)\",\n" +
                "        \"purpose\": \"\",\n" +
                "        \"format\": {\n" +
                "          \"vc+sd-jwt\": {\n" +
                "            \"sd-jwt_alg_values\": [\"ES256\", \"ES384\", \"ES512\"],\n" +
                "            \"kb-jwt_alg_values\": [\"RS256\", \"RS384\", \"RS512\", \"ES256\", \"ES384\", \"ES512\"]\n" +
                "          }\n" +
                "        },\n" +
                "        \"constraints\": {\n" +
                "          \"fields\": [\n" +
                "            { \"path\": [\"$.vct\"], \"filter\": {\"type\": \"string\", \"const\": \"urn:eu.europa.ec.eudi:ehic:1\"} },\n" +
                "            { \"path\": [\"$.subject\"], \"intent_to_retain\": false }\n" +
                "          ]\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"nonce\": \"51023777-b7fc-4a4c-b75d-e42deb79fdaf\",\n" +
                "  \"request_uri_method\": \"get\"\n" +
                "}";
    }
}
