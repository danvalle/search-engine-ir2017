/**
 * Created by dan on 17/05/17.
 */
public class Encoder {
    public String encode(int value) {
        String encodedValue = "";
        while (value >= 96) {
            encodedValue += (char) (96 + (value % 96) + 32);
            value = (value/96) - 1;

        }
        encodedValue += (char) (value + 32);

        return encodedValue;
    }


    public String encode(String value) {
        int intValue = Integer.valueOf(value);

        return encode(intValue);
    }


    public int decode(String encodedValue) {
        int i = 0;
        int b = encodedValue.charAt(i) - 32;
        int decodedValue = 0;
        int p = 1;
        while (b  >= 96) {
            decodedValue += (b - 95)*p;
            p = p * 96;
            i++;
            b = encodedValue.charAt(i) - 32;
        }
        decodedValue += (b + 1)*p;

        return decodedValue - 1;
    }


    public String decodeLine(String encodedLine) {
        String decodedLine = "";
        int i = 0;

        while (i < encodedLine.length()) {
            int b = encodedLine.charAt(i) - 32;
            int decodedValue = 0;
            int p = 1;
            while (b >= 96) {
                decodedValue += (b - 95) * p;
                p = p * 96;
                i++;
                b = encodedLine.charAt(i) - 32;
            }
            decodedValue += (b + 1) * p;

            decodedLine += decodedValue - 1;
            decodedLine += " ";
            i++;
        }

        return  decodedLine;
    }
}
