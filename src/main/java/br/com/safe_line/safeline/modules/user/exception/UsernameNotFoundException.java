package br.com.safe_line.safeline.modules.user.exception;

public class UsernameNotFoundException extends RuntimeException {
    public UsernameNotFoundException() {
        super("email not found");
    }

}