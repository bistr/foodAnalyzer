package exceptions;

import java.io.IOException;

public class UnavailableLogException extends RuntimeException {
    public UnavailableLogException(IOException e) {
    }
}
