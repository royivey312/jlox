package com.iind.lox;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {
  // SINGLE CHARACTER TOKENS
  LEFT_PAREN,
  RIGHT_PAREN,
  LEFT_BRACE,
  RIGHT_BRACE,
  COMMA,
  DOT,
  MINUS,
  PLUS,
  STAR,
  SLASH,
  SEMICOLON,

  // ONE OR TWO CHARACTER TOKENS
  BANG,
  BANG_EQUAL,
  EQUAL,
  EQUAL_EQUAL,
  GREATER,
  GREATER_EQUAL,
  LESS,
  LESS_EQUAL,

  // LITERALS
  IDENTIFIER,
  STRING,
  NUMBER,

  // KEYWORDS
  AND,
  OR,
  IF,
  ELSE,
  TRUE,
  FALSE,
  VAR,
  CLASS,
  NIL,
  FUN,
  PRINT,
  RETURN,
  SUPER,
  THIS,
  WHILE,
  FOR,

  // END OF FILE
  EOF;

  static final Map<String, TokenType> KEYWORDS = new HashMap<>();

  static {
    KEYWORDS.put("and", AND);
    KEYWORDS.put("or", OR);
    KEYWORDS.put("if", IF);
    KEYWORDS.put("else", ELSE);
    KEYWORDS.put("true", TRUE);
    KEYWORDS.put("false", FALSE);
    KEYWORDS.put("var", VAR);
    KEYWORDS.put("class", CLASS);
    KEYWORDS.put("nil", NIL);
    KEYWORDS.put("fun", FUN);
    KEYWORDS.put("print", PRINT);
    KEYWORDS.put("return", RETURN);
    KEYWORDS.put("super", SUPER);
    KEYWORDS.put("this", THIS);
    KEYWORDS.put("while", WHILE);
    KEYWORDS.put("for", FOR);
  }
}
