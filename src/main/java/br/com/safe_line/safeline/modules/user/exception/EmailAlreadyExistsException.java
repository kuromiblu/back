package br.com.safe_line.safeline.modules.user.exception;

public class EmailAlreadyExistsException extends RuntimeException{

    public EmailAlreadyExistsException() {
        super("email already exists");
    }


}
