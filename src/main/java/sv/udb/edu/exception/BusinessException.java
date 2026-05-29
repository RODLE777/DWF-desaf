package sv.udb.edu.exception;

/**
 * Excepciones personalizadas del dominio de negocio.
 * Cada excepcion mapea a un codigo HTTP especifico en el GlobalExceptionHandler.
 */

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
