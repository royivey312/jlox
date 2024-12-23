package com.iind.lox;

import com.iind.lox.Expr.Assignment;
import com.iind.lox.Expr.Binary;
import com.iind.lox.Expr.Block;
import com.iind.lox.Expr.Call;
import com.iind.lox.Expr.Get;
import com.iind.lox.Expr.Grouping;
import com.iind.lox.Expr.Literal;
import com.iind.lox.Expr.Logical;
import com.iind.lox.Expr.Set;
import com.iind.lox.Expr.Superr;
import com.iind.lox.Expr.Ternary;
import com.iind.lox.Expr.Thiss;
import com.iind.lox.Expr.Unary;
import com.iind.lox.Expr.Variable;
import com.iind.lox.Stmt.ClassDecl;
import com.iind.lox.Stmt.Expression;
import com.iind.lox.Stmt.Function;
import com.iind.lox.Stmt.IfControl;
import com.iind.lox.Stmt.Print;
import com.iind.lox.Stmt.ReturnControl;
import com.iind.lox.Stmt.Var;
import com.iind.lox.Stmt.WhileControl;

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
  public String visitLogicalExpr(Logical logical) {
    return parenthesize(logical.operator.lexeme, logical.left, logical.right);
  }

  @Override
  public String visitBinaryExpr(Binary binary) {
    return parenthesize(binary.operator.lexeme, binary.left, binary.right);
  }

  @Override
  public String visitTernaryExpr(Ternary ternary) {
    return parenthesize("?", ternary.cond, ternary.exprTrue, ternary.exprFalse);
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
  public String visitIfControlStmt(IfControl ifControl) {
    StringBuilder builder = new StringBuilder();
    builder.append("if cond:").append(parenthesize("", ifControl.cond)).append("\n");

    if (ifControl.thenBranch != null) {
      builder.append("if then:").append(ifControl.thenBranch.accept(this)).append("\n");
    }

    if (ifControl.elseBranch != null) {
      builder.append("if else:").append(ifControl.elseBranch.accept(this)).append("\n");
    }

    return builder.toString();
  }

  @Override
  public String visitPrintStmt(Print print) {
    return "print:" + print.expr.accept(this);
  }

  @Override
  public String visitBlockStmt(Stmt.Block block) {
    StringBuilder builder = new StringBuilder();
    for (Stmt statement : block.statements) {
      builder
          .append(block.statements.indexOf(statement) + 1)
          .append("=")
          .append(statement.accept(this))
          .append("\n");
    }
    return "blockStmt:" + builder.toString();
  }

  @Override
  public String visitWhileControlStmt(WhileControl whileControl) {
    return parenthesize("while", whileControl.cond) + whileControl.body.accept(this);
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

  @Override
  public String visitFunctionStmt(Function function) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitFunctionStmt'");
  }

  @Override
  public String visitCallExpr(Call call) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
  }

  @Override
  public String visitReturnControlStmt(ReturnControl returnControl) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitReturnControlStmt'");
  }

  @Override
  public String visitClassDeclStmt(ClassDecl classDecl) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitClassDeclStmt'");
  }

  @Override
  public String visitSetExpr(Set set) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitSetExpr'");
  }

  @Override
  public String visitThissExpr(Thiss thiss) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitThissExpr'");
  }

  @Override
  public String visitGetExpr(Get get) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitGetExpr'");
  }

  @Override
  public String visitSuperrExpr(Superr superr) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitSuperrExpr'");
  }
}
