package com.epam.rd.autocode.assessment.appliances.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Locale;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler({ApplianceNotFoundException.class, OrderNotFoundException.class,
            UserNotFoundException.class, ManufacturerNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(AppException ex, Model model, Locale locale, HttpServletRequest req) {
        model.addAttribute("errorCode", 404);
        model.addAttribute("errorMessage", resolve(ex.getMessageKey(), locale));
        log.warn("[404] {} id={} at {}", ex.getClass().getSimpleName(), ex.getResourceId(), req.getRequestURI());
        return "error/404";
    }

    @ExceptionHandler({ManufacturerHasAppliancesException.class, ManufacturerNameTakenException.class,
            DuplicateEmailException.class, LastAdminException.class, IllegalOrderStateException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(AppException ex, Model model, Locale locale, HttpServletRequest req) {
        model.addAttribute("errorCode", 409);
        model.addAttribute("errorMessage", resolve(ex.getMessageKey(), locale));
        log.warn("[409] {} at {}", ex.getClass().getSimpleName(), req.getRequestURI());
        return "error/409";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model, HttpServletRequest req) {
        model.addAttribute("errorCode", 403);
        model.addAttribute("errorMessage", "You do not have permission to perform this action.");
        String who = req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "anonymous";
        log.warn("[403] '{}' at {}", who, req.getRequestURI());
        return "error/403";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex, Model model, HttpServletRequest req) {
        model.addAttribute("errorCode", 400);
        model.addAttribute("errorMessage", "Invalid request parameter: " + ex.getName());
        return "error/400";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDataIntegrity(DataIntegrityViolationException ex, Model model, Locale locale, HttpServletRequest req) {
        model.addAttribute("errorCode", 409);
        model.addAttribute("errorMessage", resolve("error.data.integrity", locale));
        log.warn("[409] DataIntegrity at {}: {}", req.getRequestURI(), ex.getMessage());
        return "error/409";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model, HttpServletRequest req) {
        model.addAttribute("errorCode", 500);
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
        log.error("[500] at {}: ", req.getRequestURI(), ex);
        return "error/500";
    }

    private String resolve(String key, Locale locale) {
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (Exception e) {
            return key;
        }
    }
}
