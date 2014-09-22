/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package stanhebben.zenscript.statements;

import org.objectweb.asm.Label;
import stanhebben.zenscript.compiler.IScopeMethod;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.util.MethodOutput;
import zenscript.util.ZenPosition;

/**
 *
 * @author Stan
 */
public class StatementWhile extends Statement {
	private final Expression condition;
	private Statement contents;
	
	public StatementWhile(ZenPosition position, IScopeMethod scope, Expression condition) {
		super(position, scope);
		
		this.condition = condition;
	}
	
	public void setContents(Statement contents) {
		this.contents = contents;
	}

	@Override
	public void compile(MethodOutput output) {
		Label lblRepeat = new Label();
		Label lblBreak = new Label();
		
		output.label(lblRepeat);
		condition.compileElse(lblBreak, output);
		contents.compile(output);
		output.goTo(lblRepeat);
		output.label(lblBreak);
	}
}