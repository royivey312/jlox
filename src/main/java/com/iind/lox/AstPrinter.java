package com.iind.lox;

import com.iind.lox.Expr.Assignment;
import com.iind.lox.Expr.Binary;
import com.iind.lox.Expr.Block;
import com.iind.lox.Expr.Grouping;
import com.iind.lox.Expr.Literal;
import com.iind.lox.Expr.Ternary;
import com.iind.lox.Expr.Unary;
import com.iind.lox.Expr.Variable;
import com.iind.lox.Stmt.Expression;
import com.iind.lox.Stmt.Print;
import com.iind.lox.Stmt.Var;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

  @Override
  public String visitBlockExpr(Block block) {
    return parenthesize("block", block.expr, block.right);
  }

  @Override
  public String visitAssignmentExpr(Assignment assignment) {
    return parenthesize(String.format("assignment: %s=", assignment.name.lexeme), assignment.value);
  }

  @Override
  public String visitTernaryExpr(Ternary ternary) {
    return parenthesize("?", ternary.cond, ternary.exprTrue, ternary.exprFalse);
  }

  @Override
  public String visitBinaryExpr(Binary binary) {
    return parenthesize(binary.operator.lexeme, binary.left, binary.right);
  }

  @Override
  public String visitGroupingExpr(Grouping grouping) {
    return parenthesize("group", grouping.expression);
  }

  @Override
  public String visitVariableExpr(Variable variable) {
    return variable.name.lexeme;
  }

  @Override
  public String visitLiteralExpr(Literal literal) {
    return literal.value != null ? literal.value.toString() : "nil";
  }

  @Override
  public String visitUnaryExpr(Unary unary) {
    return parenthesize(unary.operator.lexeme, unary.right);
  }

  @Override
  public String visitExpressionStmt(Expression expression) {
    return "exprStmt:" + expression.expr.accept(this);
  }

  @Override
  public String visitBlockStmt(Stmt.Block block) {
    StringBuilder builder = new StringBuilder();
    for (Stmt statement: block.statements) {
      builder.append(block.statements.indexOf(statement) + 1)
             .append("=")
             .append(statement.accept(this))
             .append("\n");
    }
    return "blockStmt:\n" + builder.toString();
  }

  @Override
  public String visitPrintStmt(Print print) {
    return "print:" + print.expr.accept(this);
  }

  @Override
  public String visitVarStmt(Var var) {
    return "varDecl: " + var.name.lexeme + " = " + var.initializer.accept(this);
  }

  String print(Expr expr) {
    return expr.accept(this);
  }

  String print(Stmt stmt) {
    return stmt.accept(this);
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ").append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }

  public static void main(String[] args) {
    Expr expression =
        new Expr.Binary(
            new Expr.Unary(new Token(TokenType.MINUS, "-", null, 1), new Literal(123)),
            new Token(TokenType.STAR, "*", null, 1),
            new Expr.Grouping(new Expr.Literal(45.67)));

    System.out.println(new AstPrinter().print(expression));
  }
}
