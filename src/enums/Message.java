package enums;

public enum Message {
    NOT_FOUND,CANT_CONNECT,CANT_WRITE_LOG,UNKNOWN_COMMAND,MISSING_ARGUMENT,
    CANT_READ_CACHE,CANT_WRITE_CACHE,ERROR_REQUEST,FAIL_TO_INIT,CANT_OPEN_IMAGE,NO_BARCODE,ERROR, SENDING_REQUEST;

    public String toString(){
        switch(this){
            case NOT_FOUND:
                return "Product not found.";
            case CANT_CONNECT:
                return "Error connecting to server";
            case CANT_WRITE_LOG:
                return "Log unavailable for writing.";
            case UNKNOWN_COMMAND:
                return "Unknown command";
            case MISSING_ARGUMENT:
                return "Missing argument.";
            case CANT_READ_CACHE:
                return "Can't read cache. Starting server with no cache.";
            case CANT_WRITE_CACHE:
                return "Error writing cache. Not written.";
            case ERROR_REQUEST:
                return "Request couldn't be sent.";
            case CANT_OPEN_IMAGE:
                return "Image couldn't be opened for scanning.";
            case NO_BARCODE:
                return "There was no barcode in the image file.";
            case ERROR:
                return "An error occurred";
            case FAIL_TO_INIT:
                return "Failure to initialize server.";
            case SENDING_REQUEST:
                return "Sending request.";
        }
        return "Error.";
    }
}
