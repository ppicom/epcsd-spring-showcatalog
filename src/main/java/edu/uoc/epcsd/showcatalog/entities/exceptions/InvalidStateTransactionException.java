package edu.uoc.epcsd.showcatalog.entities.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidStateTransactionException extends Exception {
}
