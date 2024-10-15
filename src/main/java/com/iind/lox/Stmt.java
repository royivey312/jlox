package com.iind.lox;

public abstract class Stmt {

  interface Visitor<R> {
    R visitExpressionStmt(Expression expression);

    R visitPrintStmt(Print print);
  }

  abstract <R> R accept(Visitor<R> visitor);

  static class Expression extends Stmt {
    final Expr expr;

    Expression(Expr expr) {
      this.expr = expr;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
  }

  static class Print extends Stmt {
    final Expr expr;

    Print(Expr expr) {
      this.expr = expr;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }
  }
}
