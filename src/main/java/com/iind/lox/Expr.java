package com.iind.lox;

import java.util.List;

public abstract class Expr {

  interface Visitor<R> {
    R visitBlockExpr(Block block);
    R visitAssignmentExpr(Assignment assignment);
    R visitTernaryExpr(Ternary ternary);
    R visitBinaryExpr(Binary binary);
    R visitGroupingExpr(Grouping grouping);
    R visitLiteralExpr(Literal literal);
    R visitLogicalExpr(Logical logical);
    R visitVariableExpr(Variable variable);
    R visitUnaryExpr(Unary unary);
    R visitCallExpr(Call call);
    R visitThissExpr(Thiss thiss);
    R visitSuperrExpr(Superr superr);
    R visitSetExpr(Set set);
    R visitGetExpr(Get get);
  }

  abstract <R> R accept(Visitor<R> visitor);

  static class Block extends Expr {
    final Expr expr;
    final Expr right;

    Block(Expr expr, Expr right) {
      this.expr = expr;
      this.right = right;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockExpr(this);
    }
  }

  static class Assignment extends Expr {
    final Token name;
    final Expr value;

    Assignment(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignmentExpr(this);
    }
  }

  static class Ternary extends Expr {
    final Expr cond;
    final Expr exprTrue;
    final Expr exprFalse;

    Ternary(Expr cond, Expr exprTrue, Expr exprFalse) {
      this.cond = cond;
      this.exprTrue = exprTrue;
      this.exprFalse = exprFalse;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTernaryExpr(this);
    }
  }

  static class Binary extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
  }

  static class Grouping extends Expr {
    final Expr expression;

    Grouping(Expr expression) {
      this.expression = expression;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
  }

  static class Literal extends Expr {
    final Object value;

    Literal(Object value) {
      this.value = value;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
  }

  static class Logical extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }
  }

  static class Variable extends Expr {
    final Token name;

    Variable(Token name) {
      this.name = name;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }
  }

  static class Unary extends Expr {
    final Token operator;
    final Expr right;

    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
  }

  static class Call extends Expr {
    final Expr callee;
    final Token paren;
    final List<Expr> args;

    Call(Expr callee, Token paren, List<Expr> args) {
      this.callee = callee;
      this.paren = paren;
      this.args = args;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }
  }

  static class Thiss extends Expr {
    final Token keyword;

    Thiss(Token keyword) {
      this.keyword = keyword;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitThissExpr(this);
    }
  }

  static class Superr extends Expr {
    final Token keyword;
    final Token method;

    Superr(Token keyword, Token method) {
      this.keyword = keyword;
      this.method = method;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSuperrExpr(this);
    }
  }

  static class Set extends Expr {
    final Expr object;
    final Token name;
    final Expr value;

    Set(Expr object, Token name, Expr value) {
      this.object = object;
      this.name = name;
      this.value = value;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetExpr(this);
    }
  }

  static class Get extends Expr {
    final Expr object;
    final Token name;

    Get(Expr object, Token name) {
      this.object = object;
      this.name = name;
    }

    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetExpr(this);
    }
  }

}
