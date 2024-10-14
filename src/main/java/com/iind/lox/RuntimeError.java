package com.iind.lox;

public class RuntimeError extends RuntimeException {
  final Token operator;

  RuntimeError(Token operator, String message) {
    super(message);
    this.operator = operator;
  }


}
