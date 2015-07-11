package mvv.app.exception;


/**
 * @author Manh Vu
 */
public class HandleError extends RuntimeException {

    /** serialVersionUID */
    private static final long serialVersionUID = 3151312001268157961L;


    public HandleError(String message) {
        super(message);
    }


    /**
     * @param message
     * @param cause
     */
    public HandleError(String message, Throwable cause) {
        super(message, cause);
    }


    /**
     * @param e
     */
    public HandleError(Throwable e) {
        super(e);
    }

}
