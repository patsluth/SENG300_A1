// 	Pat Sluth
// 	SENG 300
//	30032750

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

import static org.junit.Assert.*;
import org.junit.Test;

public class A3 
{
	/**
     * Checks that a parser can be constructed for JLS3.
     */
    @Test
    public void testCreateParserForJLS() 
    {
    		assertNotNull(ASTParser.newParser(AST.JLS2));
    		assertNotNull(ASTParser.newParser(AST.JLS3));
    }

    /**
     * Checks that a parser cannot be constructed for "0", a meaningless value for it.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateParserForInvalidJSL() 
    {
    		assertNotNull(ASTParser.newParser(0));
    }
    
    /**
     * Checks that a parser correctly parses source code
     */
    @Test
    public void testParse() 
    {
    		String source = "public class Bar { public int i = 0; }";
    		String parsed ="public class Bar {\n" + 
    				"  public int i=0;\n" + 
    				"}";
    	
    		ASTParser parser = ASTParser.newParser(AST.JLS3);

	    parser.setKind(ASTParser.K_COMPILATION_UNIT);
	    parser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit)parser.createAST(null);

	    compilationUnit.accept(new ASTVisitor() {});
    		assert(compilationUnit.toString() == parsed);
    }
	
	
	
	
	
	public static void main(String[] args) 
	{
	}
}
