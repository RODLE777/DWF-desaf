package sv.udb.edu.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Integer id) {
        super(resource + " no encontrado con ID: " + id);
    }
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
