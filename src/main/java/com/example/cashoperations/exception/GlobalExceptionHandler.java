package com.example.cashoperations.exception;

import com.example.cashoperations.utils.LocalDateTimeFormatter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        List<ObjectError> validationErrorList = ex.getBindingResult().getAllErrors();

        validationErrorList.forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String validationMsg = error.getDefaultMessage();
            validationErrors.put(fieldName, validationMsg);
        });
        return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception exception,
                                                                  WebRequest webRequest) {
        return buildErrorResponse(exception, webRequest, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidDateRangeException(InvalidDateRangeException exception,
                                                                            WebRequest webRequest) {
        return buildErrorResponse(exception, webRequest, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidDepositException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidDepositException(InvalidDepositException exception,
                                                                          WebRequest webRequest) {
        return buildErrorResponse(exception, webRequest, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidAmountException(InvalidAmountException exception,
                                                                          WebRequest webRequest) {
        return buildErrorResponse(exception, webRequest, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidApiKeyException(InvalidApiKeyException exception,
                                                                         WebRequest webRequest) {
        return buildErrorResponse(exception, webRequest, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException exception,
                                                                            WebRequest webRequest) {
        return buildErrorResponse(exception, webRequest, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CurrencyNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleCurrencyNotSupportedException(CurrencyNotSupportedException exception, WebRequest webRequest) {
        return buildErrorResponse(exception, webRequest, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientDenominationException.class)
    public ResponseEntity<ErrorResponseDto> handleInsufficientDenominationException(InsufficientDenominationException exception, WebRequest webRequest) {
        return buildErrorResponse(exception, webRequest, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DenominationNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleDenominationNotFoundException(DenominationNotFoundException exception, WebRequest webRequest) {
        return buildErrorResponse(exception, webRequest, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LogBalancesException.class)
    public ResponseEntity<ErrorResponseDto> handleLogBalancesException(LogBalancesException exception, WebRequest webRequest) {
        return buildErrorResponse(exception, webRequest, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LogTransactionException.class)
    public ResponseEntity<ErrorResponseDto> handleLogTransactionException(LogTransactionException exception, WebRequest webRequest) {
        return buildErrorResponse(exception, webRequest, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponseDto> buildErrorResponse(Exception exception, WebRequest webRequest, HttpStatus status) {
        ErrorResponseDto errorResponseDTO = new ErrorResponseDto(
                webRequest.getDescription(false),
                status,
                exception.getMessage(),
                LocalDateTime.parse(LocalDateTime.now().format(LocalDateTimeFormatter.TIMESTAMP_FORMATTER), LocalDateTimeFormatter.TIMESTAMP_FORMATTER)
        );
        return new ResponseEntity<>(errorResponseDTO, status);
    }
}
