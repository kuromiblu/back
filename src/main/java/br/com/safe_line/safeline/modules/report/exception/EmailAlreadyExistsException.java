package br.com.safe_line.safeline.modules.report.exception;

public class EmailAlreadyExistsException extends RuntimeException{

    public EmailAlreadyExistsException() {
        super("email already exists");
    }


}
