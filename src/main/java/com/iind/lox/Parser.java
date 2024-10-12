package com.iind.lox;

import java.util.List;

public class Parser {
  static class ParseError extends RuntimeException {}

  final List<Token> tokens;

  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  Expr parse() {
    try {
      return block();
    } catch (ParseError pe) {
      return null;
    }
  }

  private Expr block() {
    Expr expr = expression();

    while (match(TokenType.COMMA)) {
      Expr right = expression();
      expr = new Expr.Block(expr, right);
    }

    return expr;
  }

  private Expr expression() {
    return equality();
  }

  private Expr equality() {
    Expr expr = comparison();

    while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {
    Expr expr = term();

    while (match(
        TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = binary(expr, operator, right);
    } 

    if (match(TokenType.QUESTION_MARK)) expr = ternary(expr);

    return expr;
  }

  private Expr ternary(Expr cond) {
    Expr expr = expression();

    if (match(TokenType.COLON)) {
      Expr exprFalse = expression();
      expr = new Expr.Ternary(cond, expr, exprFalse);
    } else {
      throw error(peek(), "Expected ternary termination!");
    }

    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while (match(TokenType.PLUS, TokenType.MINUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while (match(TokenType.SLASH, TokenType.STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    if (match(TokenType.BANG, TokenType.MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return primary();
  }

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

    return null;
  }

  private Expr binary(Expr left, Token operator, Expr right) {
    if (right != null) return new Expr.Binary(left, operator, right);

    throw error(peek(), String.format("Expected RHS expression for '%s' operator",  operator.lexeme));
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
