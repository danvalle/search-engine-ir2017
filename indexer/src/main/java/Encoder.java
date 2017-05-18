/**
 * Created by dan on 17/05/17.
 */
public class Encoder {
    public String encode(int value) {
        String encodedValue = "";
        while (value >= 128) {
            encodedValue += (char) (128 + (value % 128));
            value = (value/128) - 1;

        }
        encodedValue += (char) value;

        return encodedValue;
    }


    public String encode(String value) {
        int intValue = Integer.valueOf(value);

        return encode(intValue);
    }


    public int decode(String encodedValue) {
        int i = 0;
        int b = encodedValue.charAt(i);
        int decodedValue = 0;
        int p = 1;
        while (b  >= 128) {
            decodedValue += (b - 127)*p;
            p = p * 128;
            i++;
            b = encodedValue.charAt(i);
        }
        decodedValue += (b + 1)*p;

        return decodedValue - 1;
    }
}
