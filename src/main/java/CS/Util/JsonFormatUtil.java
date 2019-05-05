package CS.Util;



public class JsonFormatUtil {
    private static String SPACE = "  ";

    /*
     * Return formatted JSON from unformatted JSON
     */
    public static String formatJson(String json) {
        StringBuffer result = new StringBuffer();
        int length = json.length();
        int number = 0;
        char key = 0;
        for (int i = 0; i < length; i++) {
            // get current char
            key = json.charAt(i);
            if ((key == '[') || (key == '{')) {
                if ((i - 1 > 0) && (json.charAt(i - 1) == ':')) {
                    result.append('\n');
                    result.append(indent(number));
                }
                result.append(key);
                result.append('\n');
                number++;
                result.append(indent(number));
                continue;
            }
            if ((key == ']') || (key == '}')) {
                result.append('\n');
                number--;
                result.append(indent(number));
                result.append(key);
                if (((i + 1) < length) && (json.charAt(i + 1) != ',')) {
                    result.append('\n');
                }
                continue;
            }
            if ((key == ',')) {
                result.append(key);
                continue;
            }
            result.append(key);
        }
        return result.toString();
    }

    /**
     * Return String with given indent times
     */
    private static String indent(int number) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < number; i++) {
            result.append(SPACE);
        }
        return result.toString();
    }

    /**
     *  Remove all HTML tag in Java String
     */
    public static String purifyHTML(String str) {
        String purified = str;//StringUtils.unescapeHtml3(str);
        purified = purified.replaceAll("<[.[^>]]*>","");
        purified = purified.replace("\\n", "\t");
        return purified;
    }

    /**
     *  Replace all reserved word 'AND', 'OR', 'NOT' with their case
     */
    public static String replaceReservedWords(String str) {
        return str
                .replace("OR", "or")
                .replace("AND", "and")
                .replace("NOT", "not");
    }
}