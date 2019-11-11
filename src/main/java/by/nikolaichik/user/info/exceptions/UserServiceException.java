package by.nikolaichik.user.info.exceptions;

public class UserServiceException extends RuntimeException {

    private static final long serialVersionUID = -3772736447443688932L;

    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(String message, String userId) {
        super(String.format("%s. User id: [%s]", message, userId));
    }
}
