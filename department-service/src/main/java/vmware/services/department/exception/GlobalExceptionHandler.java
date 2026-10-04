package vmware.services.department.exception;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduz excecoes em respostas RFC 9457 (application/problem+json). Validacao e requisicao
 * malformada ja sao tratadas pela classe base.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource not found");
        return problem;
    }

    /** Falha ao chamar outro microsservico: 404 do vizinho vira 404, o resto vira 502 Bad Gateway. */
    @ExceptionHandler(FeignException.class)
    ProblemDetail handleDownstream(FeignException ex) {
        LOGGER.warn("Downstream call failed: {}", ex.getMessage());
        HttpStatus status = ex.status() == 404 ? HttpStatus.NOT_FOUND : HttpStatus.BAD_GATEWAY;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, "Downstream service call failed");
        problem.setTitle("Downstream service error");
        return problem;
    }
}
