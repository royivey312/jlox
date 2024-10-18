package com.iind.lox;

import java.util.List;

public abstract class Stmt {

  interface Visitor<R> {
    R visitExpressionStmt(Expression expression);
    R visitIfControlStmt(IfControl ifControl);
    R visitVarStmt(Var var);
    R visitBlockStmt(Block block);
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

  static class IfControl extends Stmt {
    final Expr cond;
    final Stmt thenBranch;
    final Stmt elseBranch;

    IfControl(Expr cond, Stmt thenBranch, Stmt elseBranch) {
      this.cond = cond;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfControlStmt(this);
    }
  }

  static class Var extends Stmt {
    final Token name;
    final Expr initializer;

    Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }
  }

  static class Block extends Stmt {
    final List<Stmt> statements;

    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
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
