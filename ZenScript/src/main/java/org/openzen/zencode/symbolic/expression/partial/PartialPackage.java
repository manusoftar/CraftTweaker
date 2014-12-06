/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.symbolic.expression.partial;

import org.openzen.zencode.symbolic.expression.IPartialExpression;
import java.util.Collections;
import java.util.List;
import org.openzen.zencode.symbolic.scope.IScopeMethod;
import org.openzen.zencode.symbolic.symbols.IZenSymbol;
import org.openzen.zencode.symbolic.symbols.SymbolPackage;
import org.openzen.zencode.symbolic.method.IMethod;
import org.openzen.zencode.symbolic.type.IZenType;
import org.openzen.zencode.symbolic.unit.SymbolicFunction;
import org.openzen.zencode.util.CodePosition;

/**
 *
 * @author Stanneke
 * @param <E>
 * @param <T>
 */
public class PartialPackage<E extends IPartialExpression<E, T>, T extends IZenType<E, T>>
	implements IPartialExpression<E, T>
{
	private final CodePosition position;
	private final IScopeMethod<E, T> scope;
	private final SymbolPackage<E, T> contents;

	public PartialPackage(CodePosition position, IScopeMethod<E, T> environment, SymbolPackage<E, T> contents)
	{
		this.position = position;
		this.scope = environment;
		this.contents = contents;
	}
	
	@Override
	public CodePosition getPosition()
	{
		return position;
	}
	
	@Override
	public IScopeMethod<E, T> getScope()
	{
		return scope;
	}

	@Override
	public E eval()
	{
		scope.error(position, "Cannot use package name as expression");
		return scope.getExpressionCompiler().invalid(position, scope);
	}

	@Override
	public E assign(CodePosition position, E other)
	{
		scope.error(position, "Cannot assign to a package");
		return scope.getExpressionCompiler().invalid(position, scope);
	}

	@Override
	public IPartialExpression<E, T> getMember(CodePosition position, String name)
	{
		IZenSymbol<E, T> member = contents.get(name);
		if (member == null) {
			scope.error(position, "No such member: " + name);
			return scope.getExpressionCompiler().invalid(position, scope);
		} else
			return member.instance(position, scope);
	}

	@Override
	public E call(CodePosition position, IMethod<E, T> method, E... arguments)
	{
		scope.error(position, "cannot call a package");
		return scope.getExpressionCompiler().invalid(position, scope);
	}
	
	@Override
	public E cast(CodePosition position, T type)
	{
		scope.error(position, "cannot cast a package");
		return scope.getExpressionCompiler().invalid(position, scope, type);
	}

	@Override
	public IZenSymbol<E, T> toSymbol()
	{
		return null; // not supposed to be used as symbol
	}

	@Override
	public T getType()
	{
		return null;
	}

	@Override
	public T toType(List<T> types)
	{
		scope.error(position, "not a valid type");
		return scope.getTypes().getAny();
	}

	@Override
	public List<IMethod<E, T>> getMethods()
	{
		return Collections.emptyList();
	}

	@Override
	public IPartialExpression<E, T> via(SymbolicFunction<E, T> function)
	{
		return this;
	}
}