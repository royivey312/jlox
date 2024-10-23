package com.iind.lox;

import java.util.List;

public abstract class Stmt {

  interface Visitor<R> {
    R visitExpressionStmt(Expression expression);
    R visitIfControlStmt(IfControl ifControl);
    R visitWhileControlStmt(WhileControl whileControl);
    R visitReturnControlStmt(ReturnControl returnControl);
    R visitVarStmt(Var var);
    R visitFunctionStmt(Function function);
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

  static class WhileControl extends Stmt {
    final Expr cond;
    final Stmt body;

    WhileControl(Expr cond, Stmt body) {
      this.cond = cond;
      this.body = body;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileControlStmt(this);
    }
  }

  static class ReturnControl extends Stmt {
    final Token keyword;
    final Expr res;

    ReturnControl(Token keyword, Expr res) {
      this.keyword = keyword;
      this.res = res;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnControlStmt(this);
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

  static class Function extends Stmt {
    final Token name;
    final List<Token> params;
    final List<Stmt> body;

    Function(Token name, List<Token> params, List<Stmt> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
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
