package edu.uoc.epcsd.showcatalog.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PerformanceNotFoundException extends Exception{
}
