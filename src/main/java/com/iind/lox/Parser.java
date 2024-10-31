package com.iind.lox;

import com.iind.lox.Stmt.Function;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
  static class ParseError extends RuntimeException {}

  final List<Token> tokens;

  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }
    if (Lox.OPTIONS.parserDebug) {
      debug(statements);
    }
    return statements;
  }

  private Stmt declaration() {
    try {
      if (match(TokenType.CLASS)) {
        return classDeclaration();
      }
      if (match(TokenType.FUN)) {
        return funDeclaration("function");
      }
      if (match(TokenType.VAR)) {
        return varDeclaration();
      }
      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt classDeclaration() {
    Token name = consume(TokenType.IDENTIFIER, "Expect Identifier for class declaration.");

    Expr.Variable superclass = null;
    if (match(TokenType.LESS)) {
      consume(TokenType.IDENTIFIER, "Expect super class name.");
      superclass = new Expr.Variable(previous());
    }

    consume(TokenType.LEFT_BRACE, "Expect '{' after class name.");

    List<Function> methods = new ArrayList<>();
    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
      methods.add(funDeclaration("method"));
    }

    consume(TokenType.RIGHT_BRACE, "Expect '}' at end of class body.");
    return new Stmt.ClassDecl(name, superclass, methods);
  }

  private Function funDeclaration(String kind) {
    Token name = consume(TokenType.IDENTIFIER, String.format("Expect %s name", kind));
    consume(TokenType.LEFT_PAREN, String.format("Expect '(' after %s name.", kind));

    List<Token> params = new ArrayList<>();
    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (params.size() >= 255) {
          error(peek(), "Can't have more than 255 parameters");
        }
        params.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
      } while (match(TokenType.COMMA));
    }

    consume(TokenType.RIGHT_PAREN, String.format("Expect ')' at end of %s parameter list)", kind));
    consume(TokenType.LEFT_BRACE, String.format("Expect '{' before %s body", kind));

    List<Stmt> body = blockStatement();

    return new Stmt.Function(name, params, body);
  }

  private Stmt varDeclaration() {
    Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

    Expr initializer = null;
    if (match(TokenType.EQUAL)) {
      initializer = expression();
    }

    consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
    return new Stmt.Var(name, initializer);
  }

  private Stmt statement() {
    if (match(TokenType.FOR)) {
      return forControlStatement();
    } else if (match(TokenType.IF)) {
      return ifStatement();
    } else if (match(TokenType.PRINT)) {
      return printStatement();
    } else if (match(TokenType.RETURN)) {
      return returnStatement();
    } else if (match(TokenType.WHILE)) {
      return whileStatement();
    } else if (match(TokenType.LEFT_BRACE)) {
      return new Stmt.Block(blockStatement());
    }

    return expressionStatement();
  }

  private Stmt forControlStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after if.");
    Stmt initializer = null;

    if (match(TokenType.SEMICOLON)) {
      initializer = null;
    } else if (match(TokenType.VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr cond = null;
    if (!check(TokenType.SEMICOLON)) {
      cond = expression();
    }
    consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

    Expr increment = null;
    if (!check(TokenType.SEMICOLON)) {
      increment = expression();
    }
    consume(TokenType.RIGHT_PAREN, "Expect ')' after clauses.");

    Stmt body = statement();

    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
    }

    if (cond == null) {
      cond = new Expr.Literal(true);
    }
    body = new Stmt.WhileControl(cond, body);

    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }

  private Stmt ifStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after if.");
    Expr cond = expression();
    consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

    Stmt thenBranch = statement();

    Stmt elseBranch = null;
    if (match(TokenType.ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.IfControl(cond, thenBranch, elseBranch);
  }

  private Stmt printStatement() {
    Expr expr = blockExpression();
    consume(TokenType.SEMICOLON, "Expect ';' after value.");
    return new Stmt.Print(expr);
  }

  private Stmt returnStatement() {
    Token keyword = previous();
    Expr res = null;
    if (!check(TokenType.SEMICOLON)) {
      res = expression();
    }

    consume(TokenType.SEMICOLON, "Expect ';' after return statement.");
    return new Stmt.ReturnControl(keyword, res);
  }

  private Stmt whileStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after while.");
    Expr cond = expression();
    consume(TokenType.RIGHT_PAREN, "Expect ')' after while condition.");

    Stmt body = statement();

    return new Stmt.WhileControl(cond, body);
  }

  private List<Stmt> blockStatement() {
    List<Stmt> statements = new ArrayList<>();
    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
    return statements;
  }

  private Stmt expressionStatement() {
    Expr expr = blockExpression();
    consume(TokenType.SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(expr);
  }

  private Expr blockExpression() {
    Expr expr = expression();

    while (match(TokenType.COMMA)) {
      Expr right = expression();
      expr = new Expr.Block(expr, right);
    }

    return expr;
  }

  private Expr expression() {
    return assignment();
  }

  private Expr assignment() {
    Expr expr = ternary();

    if (match(TokenType.EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable) expr).name;
        expr = new Expr.Assignment(name, value);
      } else if (expr instanceof Expr.Get) {
        Expr.Get get = (Expr.Get) expr;
        expr = new Expr.Set(get.object, get.name, value);
      } else {
        error(equals, "Invalid assignment target.");
      }
    }

    return expr;
  }

  private Expr ternary() {
    Expr expr = and();
    if (match(TokenType.QUESTION_MARK)) {
      Expr exprTrue = ternary();
      if (match(TokenType.COLON)) {
        Expr exprFalse = ternary();
        expr = new Expr.Ternary(expr, exprTrue, exprFalse);
      } else {
        throw error(peek(), "Expect ':' for ternary termination.");
      }
    }
    return expr;
  }

  private Expr and() {
    Expr expr = or();

    while (match(TokenType.AND)) {
      Token operator = previous();
      Expr right = or();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr or() {
    Expr expr = equality();

    while (match(TokenType.OR)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
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

    return call();
  }

  private Expr call() {
    Expr expr = primary();

    while (true) {
      if (match(TokenType.LEFT_PAREN)) {
        expr = finishCall(expr);
      } else if (match(TokenType.DOT)) {
        Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
        expr = new Expr.Get(expr, name);
      } else {
        break;
      }
    }

    return expr;
  }

  private Expr finishCall(Expr expr) {
    List<Expr> args = new ArrayList<>();

    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (args.size() >= 255) {
          error(peek(), "Can't have more than 255 arguments.");
        }
        args.add(expression());
      } while (match(TokenType.COMMA));
    }

    Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");

    return new Expr.Call(expr, paren, args);
  }

  private Expr primary() {
    if (match(TokenType.TRUE)) {
      return new Expr.Literal(true);
    }

    if (match(TokenType.FALSE)) {
      return new Expr.Literal(false);
    }

    if (match(TokenType.NIL)) {
      return new Expr.Literal(null);
    }

    if (match(TokenType.NUMBER, TokenType.STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(TokenType.SUPER)) {
      Token keyword = previous();
      consume(TokenType.DOT, "Expect '.' after 'super'.");
      Token method = consume(TokenType.IDENTIFIER, "Expect superclass method name.");
      return new Expr.Superr(keyword, method);
    }

    if (match(TokenType.THIS)) {
      return new Expr.Thiss(previous());
    }

    if (match(TokenType.IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    if (match(TokenType.LEFT_PAREN)) {
      Expr expr = expression();
      consume(TokenType.RIGHT_PAREN, "Expected ')' after expression!");
      return new Expr.Grouping(expr);
    }

    return null;
  }

  private Expr binary(Expr left, Token operator, Expr right) {
    if (right != null) {
      return new Expr.Binary(left, operator, right);
    }

    throw error(
        peek(), String.format("Expected RHS expression for '%s' operator", operator.lexeme));
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == TokenType.SEMICOLON) {
        return;
      }

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
    if (isAtEnd()) {
      return false;
    }

    for (TokenType type : types) {
      if (peek().type == type) {
        advance();
        return true;
      }
    }

    return false;
  }

  private boolean isAtEnd() {
    return peek().type == TokenType.EOF;
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

  private boolean check(TokenType type) {
    return peek().type == type;
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) {
      return advance();
    }

    throw error(peek(), message);
  }

  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }

  private void debug(List<Stmt> statements) {
    AstPrinter printer = new AstPrinter();
    System.out.println("Parser Output:");
    statements.forEach(
        s -> {
          if (s != null) {
            System.out.println("  " + printer.print(s));
          } else {
            System.out.println("  " + s);
          }
        });
    System.out.println();
  }
}
