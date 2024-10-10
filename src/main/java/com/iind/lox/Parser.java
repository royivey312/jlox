package com.iind.lox;

import java.util.List;

public class Parser {
  static class ParseError extends RuntimeException {}

  final List<Token> tokens;

  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  // Public interface
  Expr parse() {
    try {
      return expression();
    } catch (ParseError pe) {
      synchronize();
      return null;
    }
  }

  //  expression -> equality ;
  private Expr expression() {
    return equality();
  }

  // equality -> comparison ( ( != | == ) comparison )* ;
  private Expr equality() {
    Expr expr = comparison();
    while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  // comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term)* ;
  private Expr comparison() {
    Expr expr = term();
    while (match(
        TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  // term -> factor ( ( "-" | "+" ) factor )* ;
  private Expr term() {
    Expr expr = factor();
    while (match(TokenType.PLUS, TokenType.MINUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  // factor -> unary ( ( "/" | "*" ) unary )* ;
  private Expr factor() {
    Expr expr = unary();
    while (match(TokenType.SLASH, TokenType.STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  // unary -> ( "!" | "-" ) unary | primary ;
  private Expr unary() {
    if (match(TokenType.BANG, TokenType.MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return primary();
  }

  // primary -> NUMBER| STRING | "true" | "false" | "nil" | "(" expression ")" ;
  private Expr primary() {
    if (match(TokenType.TRUE)) return new Expr.Literal(true);
    if (match(TokenType.FALSE)) return new Expr.Literal(false);
    if (match(TokenType.NIL)) return new Expr.Literal(null);

    if (match(TokenType.NUMBER, TokenType.STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(TokenType.LEFT_PAREN)) {
      Expr expr = expression();
      consume(TokenType.RIGHT_PAREN, "Expected ')' after expression!");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expected expression!");
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == TokenType.SEMICOLON) return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case FOR:
        case WHILE:
        case IF:
        case PRINT:
        case RETURN:
        case VAR:
          return;
        default:
          break;
      }

      advance();
    }
  }

  private boolean match(TokenType... types) {
    if (isAtEnd()) return false;

    for (TokenType type : types) {
      if (peek().type == type) {
        advance();
        return true;
      }
    }

    return false;
  }

  private boolean isAtEnd() {
    return current >= tokens.size();
  }

  private Token advance() {
    return isAtEnd() ? peek() : tokens.get(current++);
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private Token consume(TokenType type, String message) {
    if (peek().type == type) return advance();

    throw error(peek(), message);
  }

  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }
}
