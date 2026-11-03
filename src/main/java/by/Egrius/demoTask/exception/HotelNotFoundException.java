package by.Egrius.demoTask.exception;

public class HotelNotFoundException extends RuntimeException {
    public HotelNotFoundException(String message) {
        super(message);
    }

    public HotelNotFoundException() {
        super();
    }
}
