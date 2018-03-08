// 	Pat Sluth
// 	SENG 300
//	30032750

import org.apache.commons.lang.ObjectUtils.Null;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import org.junit.Test;





public class A1 
{
	public A1(String filePath) throws Error, FileNotFoundException
	{
		String source = this.stringFromFile(filePath);
		System.out.println(source);
	}
	
	private String stringFromFile(String filePath) throws FileNotFoundException
    {
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        String string = "";

        while (scanner.hasNextLine()) {
        		string += scanner.nextLine();
        }

        return string;
    }
	
	
	
	
	public static void main(String[] args) 
	{
		String filePath = "/Users/patsluth/Downloads/Graph.java";
		try {
			new A1(filePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}

//		ASTParser parser = ASTParser.newParser(AST.JLS3);
//
//		parser.setKind(ASTParser.K_COMPILATION_UNIT);
//		parser.setSource(sourceCode.toCharArray());
//
//		CompilationUnit compilationUnit = (CompilationUnit)parser.createAST(null);
//		compilationUnit.accept(new ASTVisitor() 
//		{
//			 
//		});  
	}
}




